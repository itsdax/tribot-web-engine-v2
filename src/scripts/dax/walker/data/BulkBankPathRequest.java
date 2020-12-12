package scripts.dax.walker.data;


import com.allatori.annotations.DoNotRename;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@DoNotRename
@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BulkBankPathRequest {
    @DoNotRename
    private PlayerDetails player;
    @DoNotRename
    private List<BankPathRequestPair> requests;
}
