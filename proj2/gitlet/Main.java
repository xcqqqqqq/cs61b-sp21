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
            case "commit":
                if (args.length == 1) {
                    throw new GitletException("Please enter a commit message.");
                }
                Repository.commit(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "rm":
                for (int i = 1; i < args.length; i++) {
                    Repository.removeFile(args[i]);
                }
                break;
            case "log":
                Repository.printLog();
                break;
            case "global-log":
                Repository.printGlobalCommits();
                break;
            case "find":
                if (args.length == 1) {
                    throw new GitletException("Please enter a commit message to find.");
                }
                String message = args[1];
                Repository.findCommitByMessage(message);
                break;
            case "status":
                Repository.printStatus();
                break;
            case "checkout":
                if (args.length == 1) {
                    throw new GitletException("Invalid command.\nCorrect forms:\n" +
                            "checkout -- [file name]\ncheckout [commit id] -- [file name]\ncheckout [branch name]\n");
                }
                if (args.length == 2) {
                    String branchName = args[1];
                    checkoutBranch(branchName);
                }
                if (args.length == 3) {
                    String fileName = args[2];
                    checkoutFile(fileName);
                }
                if (args.length == 4) {
                    String commitID = args[1];
                    String fileName = args[3];
                    checkoutCommittedFile(commitID, fileName);
                }
                break;
            case "branch":
                if (args.length == 1) {
                    throw new GitletException("Please enter the new branch name.");
                }
                String branchName = args[1];
                Repository.newBranch(branchName);
                break;
            case "rm-branch":
                if (args.length == 1) {
                    throw new GitletException("Please enter the branch name to delete.");
                }
                String branch_name = args[1];
                Repository.deleteBranch(branch_name);
                break;
            case "reset":
                if (args.length == 1) {
                    throw new GitletException("Please enter the commit ID to reset.");
                }
                String commitID = args[1];
                Repository.reset(commitID);
                break;
            case "merge":
                if (args.length == 1) {
                    throw new GitletException("Please enter the branch name to merge.");
                }
                String branch__name = args[1];
                Repository.merge(branch__name);
                break;
        }
    }
}
