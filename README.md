# SlimEnum - 
is an enum as we know it, a fat-free alternative to Java enums. SlimEnum utilize annotations construction, but in contrast with Android [IntDef](https://developer.android.com/reference/android/support/annotation/IntDef.html) it fill annotation body to provide required enum functionality over any, even primitive(byte,int..etc.), data types. This plugin bring support SlimEnum to the IDE and
      make using it easy and efficiently. Compile by yourself or just download SlimEnum.jar and install as plugin to the Android Studio / IDEA.
SlimEnum uses annotation declaration with defined constant in their body, as named constants set for certain type. Constants type is define to wich type this constants set can be applyed. SlimEnums can be nested.

```java
      public @interface Font {
		@interface Foreground {
			int     BLACK = 0,
				RED = 1,
				GREEN = 2,
				YELLOW = 3,
				BLUE = 4,
				MAGENTA = 5,
				CYAN = 6,
				WHITE = 7,
				DEFAULT = 8;
		}
		
		@interface Background {
			int     BLACK = 0,
				RED = 1,
				GREEN = 2,
				YELLOW = 3,
				BLUE = 4,
				MAGENTA = 5,
				CYAN = 6,
				WHITE = 7,
				DEFAULT = 8,
				TRANSPARENT = 0;
		}
		
		byte    NORMAL = 0,
			BOLD = 1,
			UNDERLINE = 3,
			BLINK = 4,
			INVERSE = 5,
			STRIKE = 6;
		
		String  Helvetica = "Helvetica",
			Palatino = "Palatino",
			HonMincho = "HonMincho",
			Serif = "Serif",
			Monospaced = "Monospaced",
			Dialog = "Dialog";
	}
```

apply it to variables, fields, methods return type and metho arguments

```java
class Test {
	@Font String name;
	@Font.Foreground int fg;

	@Font.Background int setBackground(@Font.Background int bg) {
		return bg;
	}
}

static void createFont(@Font String name, @Font byte style, @Font.Background int background, @Font.Foreground int foregraund) {

}
```

and use it

```java
		createFont(Font.Monospaced + Font.HonMincho + Font.Serif, Font.BLINK, Font.Background.CYAN, Font.Foreground.BLACK);
		@Font String         name = Font.Dialog;
		@Font byte           type = Font.NORMAL | Font.BOLD | Font.INVERSE;
		@Font.Foreground int fg   = Font.Foreground.BLUE;
		
		Test test = new Test();
		test.setBackground(Font.Background.BLUE);
		test.fg = Font.Foreground.BLUE | Font.Foreground.CYAN | Font.Foreground.MAGENTA;
		
		test.fg = Font.Foreground.BLUE;
		
		if (type == (Font.NORMAL | Font.BOLD | Font.INVERSE | Font.BLINK) && test.setBackground(Font.Background.BLUE) == Font.Background.RED)
		{
			@Font.Foreground int fgw = 3;
		}
		assert (test.setBackground(Font.Background.BLUE) == (Font.Background.CYAN | Font.Background.DEFAULT));
		
		if (test.fg == Font.NORMAL && test.fg != Font.NORMAL) {
			
		}
		
		
		switch (test.setBackground(Font.Background.BLUE))
		{
			case Font.Background.BLUE:
				break;
			case Font.Background.CYAN:
				break;
			
		}
```
SlimEnum plugin can recognize applyed to variables annotation and data type, and provide correct constants set for code completion.
