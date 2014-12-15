package streams;

/**
 * Authors:
 *      Aggelos Biboudis (@biboudis)
 *      Nick Palladinos (@NickPalladinos)
 */
public class PushWithTakeFactory extends ExecPushFactory implements TakeStreamAlg<Push.t> {

    long count = 0L;

    @Override
    public <T> App<Push.t, T> take(int n, App<Push.t, T> app) {
        count = 0L;
        Push<T> f = k -> Push.prj(app).invoke(value -> {
            this.count++;
            if (count <= n) {
                k.accept(value);
            }
            // regarding the missing else part: no shortcut, this is just an example.
        });
        return f;
    }
}
