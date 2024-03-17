package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author xcq
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    /* TODO: fill in the rest of this class. */
    /** The author of this Commit. */
    private String author;

    /** The sha1HashCode of this Commit's parent. */
    private LinkedList<String> parentID;

    /** The timestamp of this Commit. */
    private String timeStamp;

    /** The tree that this Commit points to.
     *  actually a String(fileName) to String(Blob Hashcode) hashmap */
    private HashMap<String, String> table;


    public Commit(String message, String parentID, String timeStamp) {
        this.message = message;
        this.author = System.getProperty("user.name");
        this.parentID = new LinkedList<>();
        if (parentID != null) this.parentID.add(parentID);
        this.timeStamp = timeStamp;
        this.table = StagingArea.table();
    }

    /** find the Commit File by commitID and extract the Commit object from it */
    public static Commit fromID(String commitID) {
        return readObject(join(Repository.Commit_DIR, commitID), Commit.class);
    }

    public boolean hasParent() {
        return !this.parentID.isEmpty();
    }

    /** print the log information of this Commit */
    public void printCommit() {
        String printMsg = "===\ncommit " + Utils.sha1(Utils.serialize(this)) + "\n"
                + "Date: " + this.timeStamp + "\n" + this.message + "\n" + "\n";
        System.out.print(printMsg);
    }


    /** @return the first parent of this Commit */
    public Commit getFirstParent() {
        File firstParent = Utils.join(Repository.Commit_DIR, this.parentID.getFirst());
        return Utils.readObject(firstParent, Commit.class);
    }

    /** @return if the Commit's message contains the given MESSAGE */
    public boolean containsMessage(String message) {
        return this.message.contains(message);
    }

    /** store the commit object */
    public void store() {
        File commitBlob = join(Repository.Commit_DIR, sha1(serialize(this)));
        if (!commitBlob.exists()) {
            try {
                commitBlob.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeObject(commitBlob, this);
    }

    public boolean containsFile(String fileName) {
        return this.table.containsKey(fileName);
    }

    public Set<String> allTrackedFiles() {
        return this.table.keySet();
    }

    /** assert the fileName is valid */
    public void deleteFile(String fileName) {
        restrictedDelete(join(Repository.OBJECT_DIR, this.table.get(fileName)));
        restrictedDelete(fileName);
    }

    public File getBlobOf(String fileName) {
        return join(Repository.OBJECT_DIR, this.table.get(fileName));
    }

    public String getHashOf(String fileName) { return this.table.get(fileName); }

    /** merge command will create commit with multiple parents */
    public void addParent(String parentID) {
        this.parentID.add(parentID);
    }

    public int lengthFromDefault() {
        int length = 0;
        Commit p = this;
        while (p.hasParent()) {
            length += 1;
            p = p.getFirstParent();
        }
        return length;
    }
}
