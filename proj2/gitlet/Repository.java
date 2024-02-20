package gitlet;

import java.io.File;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* TODO: fill in the rest of this class. */
    /** The .gitlet/objects directory, saving Commit objects */
    public static final File OBJECT_DIR = join(GITLET_DIR, ".objects");
    /** The .gitlet/log directory, saving logs */
    public static final File LOG_DIR = join(GITLET_DIR, ".logs");


    public static void setup() {
        // prevent multiple setup
        if (GITLET_DIR.exists()) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
        // setup process
        GITLET_DIR.mkdir();
        OBJECT_DIR.mkdir();
        LOG_DIR.mkdir();
        // default commit
        Commit defaultCommit = new Commit("default Commit", "", null, "00:00:00 UTC, Thursday, 1 January 1970");
        defaultCommit.saveCommit();

        System.out.println("Initialized empty Git repository in " + GITLET_DIR.getName());
    }

    public static void addFile(String file) {

    }
}
