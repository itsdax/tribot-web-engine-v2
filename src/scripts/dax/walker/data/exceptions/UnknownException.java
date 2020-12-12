package scripts.dax.walker.data.exceptions;

import com.allatori.annotations.DoNotRename;

@DoNotRename
public class UnknownException extends RuntimeException {
    public UnknownException(String message) {
        super(message);
    }
}
