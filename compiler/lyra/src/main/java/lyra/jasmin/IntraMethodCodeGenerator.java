package lyra.jasmin;

import lyra.CodeGenerator;

/**
 *
 */
public abstract class IntraMethodCodeGenerator implements CodeGenerator {
    protected MethodHelper methodHelper;

    public void setMethodHelper(MethodHelper methodHelper) {this.methodHelper = methodHelper;}
    public MethodHelper getMethodHelper() {return this.methodHelper;}
}
