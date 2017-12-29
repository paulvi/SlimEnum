# SlimEnum - 
is an enum as we know it, a fat-free alternative to Java enums. It's utilize annotations construction to provide required enum
      functionality over any, even primitive(byte,int..etc.), data types. This plugin bring support SlimEnum to the IDE and
      make using it easy and efficiently. Compile by yourself or just download SlimEnum.jar and install as plugin to the Android Studio / IDEA.
      
    
      
      
      
      ```Java
      
      public @interface Font {
		@interface Foreground {
			int BLACK = 0,
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
			int BLACK = 0,
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
		
		byte NORMAL = 0,
				BOLD = 1,
				UNDERLINE = 3,
				BLINK = 4,
				INVERSE = 5,
				STRIKE = 6;
		
		String Helvetica = "Helvetica",
				Palatino = "Palatino",
				HonMincho = "HonMincho",
				Serif = "Serif",
				Monospaced = "Monospaced",
				Dialog = "Dialog";
	}
      
     ```
