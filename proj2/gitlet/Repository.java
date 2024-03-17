package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.*;

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
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* TODO: fill in the rest of this class. */
    /** The .gitlet/objects directory, saving Blob objects */
    public static final File OBJECT_DIR = join(GITLET_DIR, ".objects");

    /** The .gitlet/commits directory, saving Commit objects */
    public static final File Commit_DIR = join(GITLET_DIR, ".commits");

    /** Track the added files and removed files,
     * inside the file is a HashMap of (String filePath, String sha1HashCode)
     * and a set of (String filename) */
    public static final File STAGE_FILE = join(GITLET_DIR, ".index");

    /** Store the mapping between branch names and Commit Hashcode */
    public static final File BRANCH_FILE = join(GITLET_DIR, ".branches");

    /** Store the name of the current branch */
    public static final File HEAD_FILE = join(GITLET_DIR, ".head");



    /** Function for command "init" */
    public static void setup() {
        // Failure case: prevent multiple setup
        if (GITLET_DIR.exists()) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
        // setup process
        GITLET_DIR.mkdir();
        OBJECT_DIR.mkdir();
        Commit_DIR.mkdir();
        try {
            STAGE_FILE.createNewFile();
            BRANCH_FILE.createNewFile();
            HEAD_FILE.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        writeObject(STAGE_FILE, new StagingArea());

        writeObject(BRANCH_FILE, new Branches());

        writeObject(HEAD_FILE, "main");

        // default commit
        Commit defaultCommit = new Commit("initial commit",  null, "Thu Jan 01 00:00:00 1970 -0800");
        defaultCommit.store();
        Branches.putLocal("main", defaultCommit);
        // init successfully
        // System.out.println("Initialized empty Git repository in " + GITLET_DIR.getName());
    }

    /** These helper methods are all about read/write USER FILES and BLOBS */
    private static byte[] getContent(String fileName) {
        return readContents(join(CWD, fileName));
    }

    private static void copyFileAsBlob(String fileName) {
        byte[] content = getContent(fileName);
        File target = join(OBJECT_DIR, sha1(content));
        try {
            target.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeContents(target, content);
    }


    /** Function for command "add [filename]"
     * Adds a copy of the file as it currently exists to the staging area
     * Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
     * If the current working version of the file is identical to the version in the current commit,
     * do not stage it to be added, and remove it from the staging area if it is already there */
    public static void addFile(String fileName) {
        File file = join(CWD, fileName);
        // failure case: file does not exist
        if (!file.exists()) {
            throw new GitletException("File does not exist.");
        }
        
        copyFileAsBlob(fileName);
        StagingArea.stage(fileName);
    }


    /** A helper function to generate formatted time String */
    private static String getFormattedTime() {
        Date time = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-0800"));
        return sdf.format(time);
    }

    /** Function for command "commit [message]" */
    public static void commit(String msg) {
        // failure case: no file has been staged
        if (StagingArea.isAddingAreaEmpty()) {
            throw new GitletException("No changes added to the commit.");
        }

        String branchName = Branches.current();
        // read from HEAD_FILE
        Commit headCommit = Branches.currentHeadCommit();
        // create a new Commit object
        String time = getFormattedTime();
        String parentID = sha1(serialize(headCommit));
        Commit c = new Commit(msg, parentID, time);
        c.store();
        // modify BRANCH_FILE
        Branches.putLocal(branchName, c);
        // clear staging area
        StagingArea.clear();
    }


    /** Function for command "rm [filename]"
     * Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit,
     * stage it for removal and remove the file from the working directory */
    public static void removeFile(String fileName) {
        File file = join(CWD, fileName);
        // check stagingArea
        if (StagingArea.hasStaged(fileName)) {
            StagingArea.unstage(fileName);
            return;
        }
        // check head commit
        Commit headCommit = Branches.currentHeadCommit();
        if (headCommit.containsFile(fileName)) {
            headCommit.deleteFile(fileName);
            StagingArea.stageRemoval(fileName);
            return;
        }
        // failure case: the file is neither staged nor tracked by the head commit
        throw new GitletException("No reason to remove the file.");
    }


    /** Function for command "log"
     * Starting at the current head commit,
     *  display information about each commit backwards along the commit tree until the initial commit,
     * following the first parent commit links, ignoring any second parents found in merge commits. */
    public static void printLog() {
        Commit commit = Branches.currentHeadCommit();
        while(commit.hasParent()) {
            commit.printCommit();
            commit = commit.getFirstParent();
        }
        commit.printCommit();
    }


    /** Function for command "global-log"
     * Print all the commits ever made. Ordering doesn't matter */
    public static void printGlobalCommits() {
        for (String fileName : plainFilenamesIn(Commit_DIR)) {
            Commit c = readObject(join(Commit_DIR, fileName), Commit.class);
            c.printCommit();
        }
    }


    /** Function for command "find [commit message]"
     * Find all the commits that have the given message and print their ID. Ordering doesn't matter */
    public static void findCommitByMessage(String message) {
        boolean found = false;
        for (String fileName : plainFilenamesIn(Commit_DIR)) {
            Commit c = readObject(join(Commit_DIR, fileName), Commit.class);
            if (c.containsMessage(message)) {
                System.out.print("Commit " + fileName + "\n");
            }
        }
        if (!found) {
            throw new GitletException("Found no commit with that message.");
        }
    }


    /** Function for command "status"
     * Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal */
    public static void printStatus() {
        String status = "=== Branches ===\n";
        for (String branchName : Branches.localBranches()) {
            status += branchName + '\n';
        }
        status += "\n=== Staged Files ===\n";
        for (String fileName : StagingArea.stagedFiles()) {
            status += fileName + '\n';
        }
        status += "\n=== Removed Files ===\n";
        for (String fileName : StagingArea.removedFiles()) {
            status += fileName + '\n';
        }
        status += "\n=== Modifications Not Staged For Commit ===\n";
        status += "\n=== Untracked Files ===\n";
        System.out.print(status);
    }


    /** A helper method that copies [fileName] in given commit,
     * and overwrites the file with the same name in CWD.
     * If there's no file with that name in CWD, a new file will be created and written to */
    private static void checkout(Commit commit, String fileName) {
        File f = join(CWD, fileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        writeContents(f, readContents(commit.getBlobOf(fileName)));
    }


    /** Function for command "checkout -- [filename]"
     * Takes the version of the file as it exists in the head commit and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged */
    public static void checkoutFile(String fileName) {
        // failure case: FILE not tracked by head commit
        if (!Branches.currentHeadCommit().containsFile(fileName)) {
            throw new GitletException("File does not exist in that commit.");
        }

        checkout(Branches.currentHeadCommit(), fileName);
    }


    /** Function for command "checkout [commit id] -- [filename]"
     * Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged. */
    public static void checkoutCommittedFile(String commitID, String fileName) {
        // Support abbreviated CommitID: search and auto_fill the commitID (linear time)
        for (String name : plainFilenamesIn(OBJECT_DIR)) {
            if (name.startsWith(commitID)) {
                commitID = name;
            }
        }
        // failure case: Commit with commitID does not exist
        File targetCommitFile = join(Commit_DIR, commitID);
        if (!targetCommitFile.exists()) {
            throw new GitletException("No commit with that id exists.");
        }
        // failure case: FILE not tracked by head commit
        if (!Branches.currentHeadCommit().containsFile(fileName)) {
            throw new GitletException("File does not exist in that commit.");
        }

        checkout(readObject(targetCommitFile, Commit.class), fileName);
    }


    /** Function for command "checkout [branch name]"
     * Takes all files in the commit at the head of the given branch, and puts them in the working directory,
     * overwriting the versions of the files that are already there if they exist.
     * Also, at the end of this command, the given branch will now be considered the current branch
     * Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is the current branch */
    public static void checkoutBranch(String branchName) {
        // failure case: BRANCH does not exist
        if (!Branches.hasLocal(branchName)) {
            throw new GitletException("No such branch exists.");
        }
        // failure case: BRANCH is exactly the current branch
        String currentBranch = Branches.current();
        if (currentBranch.equals(branchName)) {
            throw new GitletException("No need to checkout the current branch.");
        }
        // failure case: overwriting untracked file
        Commit branch_commit = Commit.fromID(Branches.getLocal(branchName));
        Commit current_commit = Branches.currentHeadCommit();
        for (String fileName : branch_commit.allTrackedFiles()) {
            if (join(CWD, fileName).exists() && !current_commit.containsFile(fileName)) {
                throw new GitletException("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }
        
        // checkout all files in the branch
        // create and write
        for (String fileName : branch_commit.allTrackedFiles()) {
            checkout(branch_commit, fileName);
        }
        // then delete
        for (String fileName : current_commit.allTrackedFiles()) {
            if (!branch_commit.containsFile(fileName)) {
                removeFile(fileName);
            }
        }
        // change HEAD_FILE
        writeObject(HEAD_FILE, branchName);
        // clear staging area
        StagingArea.clear();
    }



    /** Function for command "branch [branch name]"
     * Creates a new branch with the given name, and points it at the current head commit. */
    public static void newBranch(String branchName) {
        // failure case: branch already exists
        if (!Branches.hasLocal(branchName)) {
            throw new GitletException("A branch with that name already exists.");
        }
        Branches.putLocal(branchName, Branches.currentHeadCommit());
    }


    /** Function for command "rm-branch" [branch name]
     * Deletes the branch with the given name.
     * only delete the pointer associated with the branch */
    public static void deleteBranch(String branchName) {
        // failure case: branch does not exist
        if (!Branches.hasLocal(branchName)) {
            throw new GitletException("A branch with that name does not exist.");
        }
        // failure case: trying to remove current branch
        if (branchName.equals(Branches.current())) {
            throw new GitletException("Cannot remove the current branch.");
        }

        Branches.removeLocal(branchName);
    }


    /** Function for command "reset [commit ID]"
     * Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit.
     * Also moves the current branch’s head to that commit node. The staging area is cleared. */
    public static void reset(String commitID) {
        // Support abbreviated CommitID: search and auto_fill the commitID (linear time)
        for (String name : plainFilenamesIn(OBJECT_DIR)) {
            if (name.startsWith(commitID)) {
                commitID = name;
            }
        }
        // checkout
        Commit commit = Commit.fromID(commitID);
        for (String fileName : commit.allTrackedFiles()) {
            checkout(commit, fileName);
        }
        // remove
        for (String fileName : StagingArea.stagedFiles()) {
            if (!commit.containsFile(fileName)) {
                removeFile(fileName);
            }
        }
        // move head to new commit
        Branches.putLocal(Branches.current(), commit);
    }


    /** A helper method that finds Blob of a commit by ID and reads the Commit object from the Blob */
    private static Commit getCommitByID(String commitID) {
        File commitBlob = join(Commit_DIR, commitID);
        return readObject(commitBlob, Commit.class);
    }

    /** A helper method that finds the latest common ancestor of two branches */
    private static Commit LCA(String branchNameA, String branchNameB) {
        Commit A = getCommitByID(Branches.getLocal(branchNameA)), B = getCommitByID(Branches.getLocal(branchNameB));
        int lengthA = A.lengthFromDefault(), lengthB = B.lengthFromDefault();
        if (lengthA > lengthB) {
            for (int i = 0; i < lengthA - lengthB; i++) {
                A = A.getFirstParent();
            }
        }
        else if (lengthA < lengthB) {
            for (int i = 0; i < lengthB - lengthA; i++) {
                B = B.getFirstParent();
            }
        }
        while (!A.equals(B)) {
            A = A.getFirstParent();
            B = B.getFirstParent();
        }
        return A;
    }

    /** A helper method that returns if a file is modified from given Commit */
    private static boolean modified(String fileName, Commit previous, Commit current) {
        if (!current.containsFile(fileName)) return true;
        String previousHash = previous.getHashOf(fileName);
        String currentHash = current.getHashOf(fileName);
        return previousHash.equals(currentHash);
    }

    /** A helper method that copies content of a file from given Commit and overwrites that file in CWD */
    private static void readAndOverwrite(String fileName, Commit c) {
        File source = c.getBlobOf(fileName);
        File target = join(CWD, fileName);
        writeContents(target, readContents(source));
    }

    /** A helper method that handles conflicts */
    private static void handleConflict(String fileName, Commit A, Commit B) {
        // file not in A and B: all deleted --> do nothing
        if (!A.containsFile(fileName) && !B.containsFile(fileName)) return;
        else if (!A.getHashOf(fileName).equals(A.getHashOf(fileName))) {
            File target = join(CWD, fileName);
            writeContents(target, readContentsAsString(A.getBlobOf(fileName)) + "=======\n" + readContentsAsString(B.getBlobOf(fileName)));
        }
    }

    /** Function for command "merge [branch name]"
     *
     * */
    public static void merge(String branchName) {
        // failure case: uncommitted changes
        if (!StagingArea.isAddingAreaEmpty() || !StagingArea.isRemovingAreaEmpty())
            throw new GitletException("You have uncommitted changes.");
        // failure case: given branch does not exist
        if (!Branches.hasLocal(branchName))
            throw new GitletException("A branch with that name does not exist.");
        // failure case: attempt to merge a branch with itself
        String headBranchName = Branches.current();
        if (branchName.equals(headBranchName))
            throw new GitletException("Cannot merge a branch with itself.");
        Commit headBranch = Branches.currentHeadCommit();
        Commit splitPoint = LCA(headBranchName, branchName);
        Commit otherBranch = Commit.fromID(Branches.getLocal(branchName));
        // special case: LCA is given branch
        if (splitPoint.equals(otherBranch)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        // special case: LCA is current branch
        if (splitPoint.equals(headBranch)) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
        }
        // normal case: has branch
        for (String fileName : splitPoint.allTrackedFiles()) {
            // normal case 1 : in split, modified in other but not head --> other
            if (modified(fileName, splitPoint, otherBranch) && !modified(fileName, splitPoint, headBranch)) {
                readAndOverwrite(fileName, otherBranch);
                StagingArea.stage(fileName);
            }
            // normal case 2: in split, modified in head but not other --> head
            else if (!modified(fileName, splitPoint, otherBranch) && modified(fileName, splitPoint, headBranch)) {
                readAndOverwrite(fileName, headBranch);
                StagingArea.stage(fileName);
            }
            // normal case 3: in split, modified in both head and other --> may conflict
            else if (modified(fileName, splitPoint, otherBranch) && modified(fileName, splitPoint, headBranch)) {
                handleConflict(fileName, headBranch, otherBranch);
            }
            // normal case 6: in split, unmodified in head, not in other --> remove
            else if (!modified(fileName, splitPoint, headBranch) && !otherBranch.containsFile(fileName)) {
                removeFile(fileName);
            }
            // normal case 7: in split, unmodified in other, not in head --> remain
        }

        for (String fileName : otherBranch.allTrackedFiles()) {
            // normal case 4: not in split, not in other, only in head --> remain
            // normal case 5: not in split, not in head, only in other --> checkout and stage
            if (!splitPoint.containsFile(fileName) && !headBranch.containsFile(fileName)) {
                checkoutFile(fileName);
                StagingArea.stage(fileName);
            }
            // normal case 8: not in split, in both head and other --> may conflict
            else if (!splitPoint.containsFile(fileName) && headBranch.containsFile(fileName)) {
                handleConflict(fileName, headBranch, otherBranch);
            }
        }
    }


}
