package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static gitlet.Utils.*;

public class StagingArea implements Serializable {
    private static final File STAGE_FILE = join(Repository.GITLET_DIR, ".index");
    private final HashMap<String, String> addingArea;
    private final HashSet<String> removingArea;

    public StagingArea() {
        this.addingArea = new HashMap<>();
        this.removingArea = new HashSet<>();
    }

    public static StagingArea readFromStageFile() {
        return readObject(STAGE_FILE, StagingArea.class);
    }

    public static boolean isAddingAreaEmpty() {
        StagingArea sa = StagingArea.readFromStageFile();
        return sa.addingArea.isEmpty();
    }

    public static boolean isRemovingAreaEmpty() {
        StagingArea sa = StagingArea.readFromStageFile();
        return sa.removingArea.isEmpty();
    }

    public static void stage(String fileName) {
        StagingArea sa = StagingArea.readFromStageFile();
        File targetFile = join(Repository.CWD, fileName);
        String hashCode = sha1(readContents(targetFile));
        sa.addingArea.put(fileName, hashCode);
        writeObject(STAGE_FILE, sa);
    }

    public static void unstage(String fileName) {
        StagingArea sa = StagingArea.readFromStageFile();
        sa.addingArea.remove(fileName);
        writeObject(STAGE_FILE, sa);
    }

    public static void stageRemoval(String fileName) {
        StagingArea sa = StagingArea.readFromStageFile();
        sa.removingArea.add(fileName);
        writeObject(STAGE_FILE, sa);
    }

    public static boolean hasStaged(String fileName) {
        StagingArea sa = StagingArea.readFromStageFile();
        return sa.addingArea.containsKey(fileName);
    }

    public static boolean hasRemoved(String fileName) {
        StagingArea sa = StagingArea.readFromStageFile();
        return sa.removingArea.contains(fileName);
    }


    public static HashMap<String, String> table() {
        StagingArea sa = StagingArea.readFromStageFile();
        return sa.addingArea;
    }

    public static Set<String> stagedFiles() {
        StagingArea sa = StagingArea.readFromStageFile();
        return sa.addingArea.keySet();
    }

    public static Set<String> removedFiles() {
        StagingArea sa = StagingArea.readFromStageFile();
        return sa.removingArea;
    }

    public static void clear() {
        writeObject(STAGE_FILE, new StagingArea());
    }
}
