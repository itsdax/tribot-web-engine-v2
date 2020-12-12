package scripts.dax.walker.data;

import com.allatori.annotations.DoNotRename;

@DoNotRename
public interface Requirement {
    boolean satisfies();
}
