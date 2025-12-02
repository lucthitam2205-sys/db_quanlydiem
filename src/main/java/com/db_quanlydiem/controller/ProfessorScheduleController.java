package com.db_quanlydiem.controller;

import com.db_quanlydiem.dao.ClassScheduleDAO;
import com.db_quanlydiem.dao.CourseClassDAO;
import com.db_quanlydiem.dao.SubjectDAO;
import com.db_quanlydiem.model.ClassSchedule;
import com.db_quanlydiem.model.CourseClass;
import com.db_quanlydiem.model.Subject;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProfessorScheduleController implements Initializable {

    @FXML private TableView<ScheduleViewModel> tableSchedule;
    @FXML private TableColumn<ScheduleViewModel, String> colDay, colShift, colClassID, colSubjectName, colRoom;

    private ClassScheduleDAO scheduleDAO = new ClassScheduleDAO();
    private CourseClassDAO courseClassDAO = new CourseClassDAO();
    private SubjectDAO subjectDAO = new SubjectDAO();

    private String professorID;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDay.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDay()));
        colShift.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getShift()));
        colClassID.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getClassID()));
        colSubjectName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSubjectName()));
        colRoom.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRoom()));
    }

    public void setProfessorID(String professorID) {
        this.professorID = professorID;
        loadSchedule();
    }

    private void loadSchedule() {
        if (professorID == null) return;

        List<ClassSchedule> schedules = scheduleDAO.getSchedulesByProfessor(professorID);
        List<CourseClass> allClasses = courseClassDAO.getAllCourseClasses();
        List<Subject> allSubjects = subjectDAO.getAllSubjects();

        ObservableList<ScheduleViewModel> viewList = FXCollections.observableArrayList();

        for (ClassSchedule sch : schedules) {
            // Tìm thông tin Lớp
            CourseClass courseClass = allClasses.stream()
                    .filter(c -> c.getCourseClassId().equals(sch.getCourseClassID()))
                    .findFirst().orElse(null);

            String subName = "N/A";
            if (courseClass != null) {
                // Tìm thông tin Môn
                Subject subject = allSubjects.stream()
                        .filter(s -> s.getSubjectId().equals(courseClass.getSubjectId()))
                        .findFirst().orElse(null);
                if (subject != null) subName = subject.getSubjectName();
            }

            viewList.add(new ScheduleViewModel(
                    sch.getDayOfWeek(),
                    sch.getShift(),
                    sch.getCourseClassID(),
                    subName,
                    sch.getRoom()
            ));
        }

        tableSchedule.setItems(viewList);
    }

    @FXML
    public void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    // ViewModel
    public static class ScheduleViewModel {
        private final String day, shift, classID, subjectName, room;

        public ScheduleViewModel(String day, String shift, String classID, String subjectName, String room) {
            this.day = day; this.shift = shift; this.classID = classID;
            this.subjectName = subjectName; this.room = room;
        }
        public String getDay() { return day; }
        public String getShift() { return shift; }
        public String getClassID() { return classID; }
        public String getSubjectName() { return subjectName; }
        public String getRoom() { return room; }
    }
}