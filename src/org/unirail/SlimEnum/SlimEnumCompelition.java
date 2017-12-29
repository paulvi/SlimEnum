package org.unirail.SlimEnum;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.VariableLookupItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import java.util.stream.Collectors;

public class SlimEnumCompelition extends CompletionContributor {
	public SlimEnumCompelition() {
		extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<CompletionParameters>() {
			public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet resultSet) {
				
				final PsiElement position   = parameters.getOriginalPosition();
				final int        pos_offset = position.getTextOffset();
				final int        pos        = parameters.getPosition().getTextOffset();
				
				PsiElement scope = PsiTreeUtil.getParentOfType(parameters.getPosition(),
						PsiAssignmentExpression.class,
						PsiDeclarationStatement.class,
						PsiMethodCallExpression.class,
						PsiBinaryExpression.class,
						PsiSwitchStatement.class);
				
				if (scope == null) scope = PsiTreeUtil.getParentOfType(position,
						PsiAssignmentExpression.class,
						PsiDeclarationStatement.class,
						PsiMethodCallExpression.class,
						PsiBinaryExpression.class,
						PsiSwitchStatement.class);
				
				if (scope == null) return;
				
				if (scope instanceof PsiAssignmentExpression)//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
				{
					PsiElement ae = ((PsiReferenceExpression) ((PsiAssignmentExpression) scope).getLExpression()).resolve();
					
					if (ae instanceof PsiVariable)
					{
						PsiAnnotation[] annotations = AnnotationUtil.getAllAnnotations((PsiVariable) ae, true, null);
						if (annotations.length == 0) return;
						
						fill(resultSet, annotations, ((PsiVariable) ae).getType(), excludes(((PsiAssignmentExpression) scope).getRExpression()));
					}
					return;
				}
				
				if (scope instanceof PsiDeclarationStatement)//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
				{
					PsiElement[]    vars        = ((PsiDeclarationStatement) scope).getDeclaredElements();
					PsiAnnotation[] annotations = AnnotationUtil.getAllAnnotations((PsiVariable) vars[0], true, null);
					if (annotations.length == 0) return;
					
					for (PsiElement v : vars)
						if (pos <= v.getTextRange().getEndOffset())
						{
							PsiVariable pv = (PsiVariable) v;
							fill(resultSet, annotations, pv.getType(), excludes(pv.getInitializer()));
							return;
						}
				}
				
				if (scope instanceof PsiBinaryExpression)
				{
					PsiBinaryExpression pbe = (PsiBinaryExpression) scope;// PsiTreeUtil.getParentOfType(position, PsiBinaryExpression.class);
					
					PsiAnnotation[] annotations = null;
					PsiType         type        = null;
					
					Collection<PsiReferenceExpression> ref = PsiTreeUtil.findChildrenOfType(pbe.getLOperand(), PsiReferenceExpression.class);
					
					if (0 < ref.size())
					{
						PsiElement elem = ref.iterator().next().resolve();
						if (elem instanceof PsiVariable)
						{
							PsiVariable dst = (PsiVariable) elem;
							annotations = AnnotationUtil.getAllAnnotations(dst, true, null);
							if (annotations.length == 0) return;
							type = dst.getType();
						}
						else
							if (elem instanceof PsiMethod)
							{
								PsiMethod dst = (PsiMethod) elem;
								annotations = AnnotationUtil.getAllAnnotations(dst, true, null);
								if (annotations.length == 0) return;
								type = dst.getReturnType();
							}
							else
							{
								tryPsiMethodCallExpression(position, pos, resultSet);
								return;
							}
						fill(resultSet, annotations, type, excludes(pbe.getROperand()));
					}
					return;
				}
				
				
				if (scope instanceof PsiMethodCallExpression)//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
				{
					tryPsiMethodCallExpression(position, pos, resultSet);
					return;
				}
				
				
				if (scope instanceof PsiSwitchStatement)//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
				{
					PsiSwitchStatement sw = (PsiSwitchStatement) scope;
					
					PsiElement[] refs = PsiTreeUtil.collectElements(sw.getExpression(), el -> el instanceof PsiReferenceExpression);
					if (refs == null) return;
					
					PsiAnnotation[] annotations = null;
					PsiType         type        = null;
					
					for (PsiElement ref : refs)
					{
						PsiElement elem = ((PsiReferenceExpression) ref).resolve();
						if (elem instanceof PsiVariable)
						{
							PsiVariable dst = (PsiVariable) elem;
							annotations = AnnotationUtil.getAllAnnotations(dst, true, null);
							if (annotations.length == 0) annotations = null;
							else
							{
								type = dst.getType();
								break;
							}
						}
						else
							if (elem instanceof PsiMethod)
							{
								PsiMethod dst = (PsiMethod) elem;
								annotations = AnnotationUtil.getAllAnnotations(dst, true, null);
								if (annotations.length == 0) annotations = null;
								else
								{
									type = dst.getReturnType();
									break;
								}
							}
					}
					if (annotations == null) return;
					
					Vector<String> exclude = new Vector<>();
					boolean        show    = false;
					for (PsiStatement sw_state : sw.getBody().getStatements())
						if (sw_state instanceof PsiSwitchLabelStatement)
						{
							TextRange textRange = sw_state.getTextRange();
							if (textRange.getStartOffset() < pos_offset && pos_offset < textRange.getEndOffset()) show = true;
							
							PsiSwitchLabelStatement sls       = (PsiSwitchLabelStatement) sw_state;
							PsiExpression           caseValue = sls.getCaseValue();
							if (caseValue instanceof PsiReferenceExpression)
								exclude.add(((PsiReferenceExpression) caseValue).getQualifiedName());
						}
					
					if (show) fill(resultSet, annotations, type, exclude);
				}
			}
		});
	}
	
	
	void tryPsiMethodCallExpression(PsiElement position, int pos, CompletionResultSet resultSet) {
		PsiMethodCallExpression mce = PsiTreeUtil.getParentOfType(position, PsiMethodCallExpression.class);
		if (mce == null) return;
		
		PsiExpression[] expressions = mce.getArgumentList().getExpressions();
		
		final PsiMethod method = (PsiMethod) mce.getMethodExpression().resolve();
		if (method == null) return;
		PsiParameter[] method_params = method.getParameterList().getParameters();
		if (method_params.length == 0) return;
		
		int idx = 0;
		if (0 < expressions.length)
			while (pos <= expressions[idx].getTextRange().getEndOffset() && idx < expressions.length - 1) idx++;
		
		
		PsiAnnotation[] param_annotations = AnnotationUtil.getAllAnnotations(method_params[idx], true, null);
		if (0 < param_annotations.length) fill(resultSet, param_annotations, method_params[idx].getType(), expressions.length == 0 ? null : excludes(expressions[idx]));
	}
	
	
	static Iterable<String> excludes(PsiElement exclude_expr) {
		PsiElement[] ex = exclude_expr == null ? null : PsiTreeUtil.collectElements(exclude_expr, element ->
				element instanceof PsiReferenceExpression && element.getText().contains("."));
		return ex == null ? null : Arrays.stream(ex).map(e -> ((PsiReferenceExpression) e).getQualifiedName()).collect(Collectors.toList());
	}
	
	
	public static void fill(CompletionResultSet dst, PsiAnnotation[] anns, PsiType type, Iterable<String> excludes) {
		if (anns == null || anns.length == 0) return;
		
		boolean stop = false;
		for (PsiAnnotation ann : anns)
		{
			Project      project       = ann.getProject();
			final String qualifiedName = ann.getQualifiedName();
			
			PsiClass annotation = JavaPsiFacade.getInstance(project).findClass(qualifiedName, GlobalSearchScope.allScope(project));
			if (annotation == null) continue;
			
			PsiField[] fields = annotation.getFields();
			if (fields.length == 0) return;
			
			String type_str = type.getCanonicalText();
			for (PsiField fld : fields)
				if (fld.getType().getCanonicalText().equals(type_str))
					tonext:{
						
						String name = annotation.getQualifiedName() + "." + fld.getName();
						
						if (excludes != null)
							for (String exc : excludes)
							{
								if (name.endsWith(exc))
									break tonext;
							}
						
						stop = true;
						VariableLookupItem variableLookupItem = new VariableLookupItem(fld, false) {
							@Override public void renderElement(LookupElementPresentation presentation) {
								super.renderElement(presentation);
								presentation.setTailText(" = " + fld.getInitializer().getText(), true);
							}
						};
						
						dst.addElement(variableLookupItem);
					}
		}
	}
}
