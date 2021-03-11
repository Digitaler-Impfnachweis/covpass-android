# Debug build rules for apps. This allows testing obfuscation and still allows running the debugger.

-keepattributes SourceFile,LineNumberTable
-keepattributes LocalVariableTable,LocalVariableTypeTable
#-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
#-keepparameternames
#-dontoptimize
#-dontobfuscate
