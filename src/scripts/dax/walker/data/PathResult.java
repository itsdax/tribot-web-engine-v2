package scripts.dax.walker.data;

import com.allatori.annotations.DoNotRename;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@DoNotRename
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PathResult {
    @DoNotRename
    private PathStatus pathStatus;
    @DoNotRename
    private List<Point3D> path;
    @DoNotRename
    private int cost;
}

