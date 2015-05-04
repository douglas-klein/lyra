package lyra.scopes;

/**
 * Created by eduardo on 29/04/15.
 */
public class LocalScope extends BaseScope {
    public LocalScope(Scope parent) { super(parent); }
    public String getScopeName() { return "local"; }
}
