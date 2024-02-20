package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class

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
    public String author;
    public String parentID;
    public String timeStamp;

    public Commit(String m, String a, String p, String t) {
        this.message = m;
        this.author = a;
        this.parentID = p;
        this.timeStamp = t;
    }

    /** Apply sha1 hash on the COMMIT instance,
     * and save the serialized instance in OBJECT_DIR */
    public File saveCommit() {
        String sha1code = Utils.sha1(Utils.serialize(this));
        String sha1first2chars = sha1code.substring(0, 1);
        File folder = Utils.join(Repository.OBJECT_DIR, sha1first2chars);
        folder.mkdir();
        String fileName = sha1code.substring(2);
        File commitFile = Utils.join(folder, fileName);
        try {
            commitFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Utils.writeObject(commitFile, this);
        return commitFile;
    }

}
