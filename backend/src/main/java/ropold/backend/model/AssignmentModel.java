package ropold.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private DeviceModel device;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeModel employee;

    @ManyToOne
    @JoinColumn(name = "handed_out_by")
    private EmployeeModel handedOutBy;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Column(name = "returned_date")
    private LocalDate returnedDate;

    @Column(name = "condition_out", columnDefinition = "TEXT")
    private String conditionOut;

    @Column(name = "condition_in", columnDefinition = "TEXT")
    private String conditionIn;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "copy_handed_to_employee", nullable = false)
    private boolean copyHandedToEmployee = false;

    @Column(name = "copy_filed_in_personnel_file", nullable = false)
    private boolean copyFiledInPersonnelFile = false;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentFileModel> files = new ArrayList<>();
}