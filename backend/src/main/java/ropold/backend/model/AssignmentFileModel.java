package ropold.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assignment_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentFileModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private AssignmentModel assignment;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}