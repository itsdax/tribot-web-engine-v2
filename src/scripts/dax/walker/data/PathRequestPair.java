package scripts.dax.walker.data;

import com.allatori.annotations.DoNotRename;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@DoNotRename
@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PathRequestPair {
    @DoNotRename
    private Point3D start;
    @DoNotRename
    private Point3D end;
}
