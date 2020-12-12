package scripts.dax.walker.data.exceptions;

import com.allatori.annotations.DoNotRename;

@DoNotRename
public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}
