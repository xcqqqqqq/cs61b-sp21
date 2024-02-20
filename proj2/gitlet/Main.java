package gitlet;
import gitlet.Repository.*;
import gitlet.Utils.*;

import static gitlet.Repository.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author xcq
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                Repository.setup();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                for (int i = 1; i < args.length; i++) {
                    Repository.addFile(args[i]);
                }
                break;
            // TODO: FILL THE REST IN
        }
    }
}
