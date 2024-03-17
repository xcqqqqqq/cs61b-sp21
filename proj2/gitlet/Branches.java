package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import static gitlet.Utils.*;

public class Branches implements Serializable {
    private static final File BRANCH_FILE = join(Repository.GITLET_DIR, ".branches");

    private final HashMap<String, String> local_branches;

    public Branches() {
        this.local_branches = new HashMap<>();
    }

    public static Branches readFromBranchFile() {
        return readObject(BRANCH_FILE, Branches.class);
    }

    public static String current() {
        return readObject(Repository.HEAD_FILE, String.class);
    }

    public static Commit currentHeadCommit() {
        File commitFile = join(Repository.Commit_DIR, getLocal(current()));
        return readObject(commitFile, Commit.class);
    }

    public static void putLocal(String branchName, Commit commit) {
        Branches bs = readFromBranchFile();
        String hashCode = sha1(serialize(commit));
        bs.local_branches.put(branchName, hashCode);
        writeObject(BRANCH_FILE, bs);
    }

    public static void removeLocal(String branchName) {
        Branches bs = readFromBranchFile();
        bs.local_branches.remove(branchName);
        writeObject(BRANCH_FILE, bs);
    }

    public static Set<String> localBranches() {
        Branches bs = readFromBranchFile();
        return bs.local_branches.keySet();
    }

    public static boolean hasLocal(String branchName) {
        Branches bs = readFromBranchFile();
        return bs.local_branches.containsKey(branchName);
    }

    public static String getLocal(String branchName) {
        Branches bs = readFromBranchFile();
        return bs.local_branches.get(branchName);
    }

}
