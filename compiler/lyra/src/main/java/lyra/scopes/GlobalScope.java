package lyra.scopes;

/**
 * Created by eduardo on 29/04/15.
 */
public class GlobalScope extends BaseScope {
    public GlobalScope() { super(null); }
    public String getScopeName() { return "global"; }
}
