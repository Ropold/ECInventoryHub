package ropold.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "device_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceFileModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private DeviceModel device;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}