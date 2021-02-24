package ovh.roro.wankil.deathswap.util.reflect;

/**
 * An interface for invoking a specific constructor.
 */
public interface ConstructorInvoker
{
    /**
     * Invoke a constructor for a specific class.
     *
     * @param arguments - the arguments to pass to the constructor.
     * @return The constructed object.
     */
    Object invoke(Object... arguments);
}