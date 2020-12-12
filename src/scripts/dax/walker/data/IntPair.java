package scripts.dax.walker.data;

import com.allatori.annotations.DoNotRename;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@DoNotRename
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IntPair {
    @DoNotRename
    private int key;
    @DoNotRename
    private int value;
}
