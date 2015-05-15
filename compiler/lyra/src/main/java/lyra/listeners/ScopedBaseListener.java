package lyra.listeners;

import lyra.LyraParser;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Helper base class that, for all scope-creating rules, calls abstract methods
 * when entering and exiting those rules.
 *
 * This allows for a single pair of beginScopeVisit() endScopeVisit() instead of
 * overriding methods for all rules.
 */
public abstract class ScopedBaseListener extends lyra.LyraParserBaseListener {

    @Override
    public void enterMethod_decl(LyraParser.Method_declContext ctx) {
        beginScopeVisit(true, ctx);
    }
    @Override
    public void exitMethod_decl(LyraParser.Method_declContext ctx) {
        endScopeVisit(true, ctx);
    }

    @Override
    public void enterMethod_decl_abstract(LyraParser.Method_decl_abstractContext ctx) {
        beginScopeVisit(true, ctx);
    }
    @Override
    public void exitMethod_decl_abstract(LyraParser.Method_decl_abstractContext ctx) {
        endScopeVisit(true, ctx);
    }

    @Override
    public void enterClassdecl(LyraParser.ClassdeclContext ctx) {
        beginScopeVisit(true, ctx);
    }
    @Override
    public void exitClassdecl(LyraParser.ClassdeclContext ctx) {
        endScopeVisit(true, ctx);
    }

    @Override
    public void enterInterfacedecl(LyraParser.InterfacedeclContext ctx) {
        beginScopeVisit(true, ctx);
    }
    @Override
    public void exitInterfacedecl(LyraParser.InterfacedeclContext ctx) {
        endScopeVisit(true, ctx);
    }

    @Override
    public void enterScopestat(LyraParser.ScopestatContext ctx) {
        beginScopeVisit(false, ctx);
    }
    @Override
    public void exitScopestat(LyraParser.ScopestatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterForstat(LyraParser.ForstatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitForstat(LyraParser.ForstatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterWhilestat(LyraParser.WhilestatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitWhilestat(LyraParser.WhilestatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterForever(LyraParser.ForeverContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitForever(LyraParser.ForeverContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterSwitchstat(LyraParser.SwitchstatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitSwitchstat(LyraParser.SwitchstatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterIfstat(LyraParser.IfstatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitIfstat(LyraParser.IfstatContext ctx) {
        endScopeVisit(false, ctx);
    }

    @Override
    public void enterElsestat(LyraParser.ElsestatContext ctx) {
        beginScopeVisit(false, ctx);
    }

    @Override
    public void exitElsestat(LyraParser.ElsestatContext ctx) {
        endScopeVisit(false, ctx);
    }

    protected abstract void beginScopeVisit(boolean named, ParserRuleContext ctx);
    protected abstract void endScopeVisit(boolean named, ParserRuleContext ctx);
}
