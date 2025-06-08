package common;

import java.util.ArrayList;
import java.util.List;

public class GroupInfo {
    public List<String> members = new ArrayList<>();
    public String groupBio;

    public GroupInfo(List<String> members, String groupBio) {
        this.members = members;
        this.groupBio = groupBio;
    }

    public List<String> getMembers() {
        return members;
    }

    public String getGroupBio() {
        return groupBio;
    }
}
