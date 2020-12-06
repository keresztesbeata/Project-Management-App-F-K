import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class MainMenu extends JMenuBar implements ActionListener {
    private JMenu accountMenu = new JMenu("My account");
    private JMenu teamsMenu = new JMenu("My teams");

    // Items for accountMenu.
    private JMenuItem accountSettingsItem = new JMenuItem("Account Settings");
    private JMenuItem logoutItem = new JMenuItem("Log out");

    // Items for teamsMenu.
    private JMenuItem createTeamItem = new JMenuItem("Create new team");
    private JMenuItem joinTeamItem = new JMenuItem("Join team");

    private MainMenuController controller = new MainMenuController();

    public MainMenu() {
        accountMenu.add(accountSettingsItem);
        accountMenu.add(logoutItem);

        teamsMenu.add(createTeamItem);
        teamsMenu.add(joinTeamItem);

        this.add(accountMenu);
        this.add(teamsMenu);

        addListeners();
        setMnemonics();
    }

    private void setMnemonics() {
        accountMenu.setMnemonic(KeyEvent.VK_A);
        teamsMenu.setMnemonic(KeyEvent.VK_T);

        accountSettingsItem.setMnemonic(KeyEvent.VK_S);
        logoutItem.setMnemonic((KeyEvent.VK_L));

        createTeamItem.setMnemonic(KeyEvent.VK_C);
        joinTeamItem.setMnemonic(KeyEvent.VK_J);
    }

    private void addListeners() {
        accountSettingsItem.addActionListener(this);
        logoutItem.addActionListener(this);
        createTeamItem.addActionListener(this);
        joinTeamItem.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == accountSettingsItem) {
            controller.enableUserDataSettings();
        } else if (actionEvent.getSource() == logoutItem) {
            controller.logoutUser();
        } else if (actionEvent.getSource() == createTeamItem) {
            controller.enableCreatingNewTeam();
        } else if (actionEvent.getSource() == joinTeamItem) {
            controller.enableJoiningNewTeam();
        }
    }
}