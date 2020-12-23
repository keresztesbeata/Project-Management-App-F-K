package view.project;

import controller.project.CreateProjectController;
import model.team.Team;
import model.user.User;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import view.UIFactory;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class CreateProjectFrame extends JFrame {

    private JButton saveButton;

    private JDatePickerImpl deadlineDatePicker;
    private JDatePanelImpl datePanel;
    private UtilDateModel dateModel;

    private JTextField titleTextField;
    private JTextArea descriptionTextArea;
    private JScrollPane descriptionScrollPane;

    private JComboBox<String> teamComboBox;
    private DefaultComboBoxModel<String> teamModel;
    private JComboBox<String> assigneeComboBox;
    private DefaultComboBoxModel<String> assigneeModel;

    private JFrame parentFrame;
    private CreateProjectController controller;

    private static final Dimension DIMENSION = new Dimension(600, 600);


    public CreateProjectFrame(JFrame parentFrame) {
        super("New Project");
        this.parentFrame = parentFrame;
        controller = new CreateProjectController(this);
        this.setMinimumSize(DIMENSION);
        this.setLayout(new BorderLayout());
        initComponents();
        this.setResizable(false);
        this.addWindowListener(new ProjectWindowAdapter());
        this.setVisible(true);
    }

    private void initDatePicker() {
        dateModel = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");
        datePanel = new JDatePanelImpl(dateModel, properties);
        deadlineDatePicker = new JDatePickerImpl(datePanel, new DefaultFormatter());
    }

    private void initDescriptionTextArea() {
        descriptionTextArea = new JTextArea();
        descriptionTextArea.setText(null);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionScrollPane = new JScrollPane(descriptionTextArea);
    }

    private void initTeamsComboBox() {
        teamComboBox = new JComboBox<>();
        teamModel = new DefaultComboBoxModel<>();
        List<Team> teamList = controller.getTeamsOfUser();
        if(teamList!=null) {
            for(Team team:teamList) {
                teamModel.addElement(team.getName());
            }
        }
        teamComboBox.setModel(teamModel);
        teamComboBox.addActionListener(e -> {
            String team = teamComboBox.getModel().getSelectedItem().toString();
            int teamId = controller.getIdOfTeam(team);
            if(teamId > 0) {
                updateAssigneeModel(teamId);
            }
        });
    }

    private void initAssigneeComboBox() {
        assigneeComboBox = new JComboBox<>();
        assigneeModel = new DefaultComboBoxModel<>();
        assigneeComboBox.setModel(assigneeModel);
    }

    private void updateAssigneeModel(int teamId) {
        assigneeModel.removeAllElements();
        List<User> members = controller.getTeamMembers(teamId);
        if (members != null) {
            for (User member : members) {
                assigneeModel.addElement(member.getUsername());
            }
        }
    }

    private void initDataFields() {
        titleTextField = UIFactory.createTextField(null);
        initDatePicker();
        initDescriptionTextArea();
        initAssigneeComboBox();
        initTeamsComboBox();
    }
    private void initComponents() {
        initDataFields();
        initContentPanel();
        initButtonsPanel();
    }
    private void initContentPanel() {
        JPanel contentPanel = new JPanel();
        GroupLayout contentLayout = new GroupLayout(contentPanel);
        contentLayout.setAutoCreateGaps(true);
        contentLayout.setAutoCreateContainerGaps(true);
        contentPanel.setLayout(contentLayout);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JLabel titleLabel = UIFactory.createLabel("Title:", null);
        JLabel teamLabel = UIFactory.createLabel("Team:",null);
        JLabel deadlineLabel = UIFactory.createLabel("Deadline:", null);
        JLabel descriptionLabel = UIFactory.createLabel("Description:", null);
        JLabel assigneeLabel = UIFactory.createLabel("Assignee:", null);

        contentLayout.setHorizontalGroup(
                contentLayout
                        .createParallelGroup()
                        .addGroup(
                                contentLayout
                                        .createSequentialGroup()
                                        .addGroup(
                                                contentLayout
                                                        .createParallelGroup()
                                                        .addComponent(titleLabel)
                                                        .addComponent(teamLabel)
                                                        .addComponent(deadlineLabel)
                                                        .addComponent(descriptionLabel)
                                                        .addComponent(assigneeLabel))
                                        .addGroup(
                                                contentLayout
                                                        .createParallelGroup()
                                                        .addComponent(titleTextField)
                                                        .addComponent(teamComboBox)
                                                        .addComponent(datePanel)
                                                        .addComponent(descriptionScrollPane)
                                                        .addComponent(assigneeComboBox))));
        contentLayout.setVerticalGroup(
                contentLayout
                        .createSequentialGroup()
                        .addGroup(
                                contentLayout
                                        .createParallelGroup()
                                        .addComponent(titleLabel)
                                        .addComponent(titleTextField))
                        .addGroup(
                                contentLayout
                                        .createParallelGroup()
                                        .addComponent(teamLabel)
                                        .addComponent(teamComboBox))
                        .addGroup(
                                contentLayout
                                        .createParallelGroup()
                                        .addComponent(deadlineLabel)
                                        .addComponent(datePanel))
                        .addGroup(
                                contentLayout
                                        .createParallelGroup()
                                        .addComponent(descriptionLabel)
                                        .addComponent(descriptionScrollPane, 80, 80, 80))
                        .addGroup(
                                contentLayout
                                        .createParallelGroup()
                                        .addComponent(assigneeLabel)
                                        .addComponent(assigneeComboBox)));
        this.add(contentPanel, BorderLayout.CENTER);
    }

    private void initButtonsPanel() {
        saveButton = UIFactory.createButton("Save Project");
        saveButton.addActionListener(e -> {
            String title = titleTextField.getText();
            String team = teamComboBox.getModel().getSelectedItem().toString();
            String assignee = Objects.requireNonNull(assigneeComboBox.getSelectedItem()).toString();
            LocalDate deadline =
                    LocalDate.of(
                            deadlineDatePicker.getModel().getYear(),
                            deadlineDatePicker.getModel().getMonth(),
                            deadlineDatePicker.getModel().getDay());
            String description = descriptionTextArea.getText();
            controller.createProject(title,team,assignee,deadline,description);
        });

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buttonsPanel.add(saveButton);
        this.add(buttonsPanel, BorderLayout.SOUTH);
    }

    class ProjectWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            controller.onClose(parentFrame);
        }
    }
}