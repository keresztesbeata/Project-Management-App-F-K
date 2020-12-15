package model.project;

public class DuplicateProjectNameException extends Exception {
    public DuplicateProjectNameException(String projectName, String teamName) {
        super("Invalid project name: a project with name " + projectName + " already exists in " +
                "team " + teamName);
    }
}