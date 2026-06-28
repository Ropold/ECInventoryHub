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
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DeviceType type;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "serial_number", unique = true)
    private String serialNumber;

    @Column(name = "inventory_number", unique = true)
    private String inventoryNumber;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviceStatus status = DeviceStatus.AVAILABLE;

    @Column(name = "defective", nullable = false)
    private boolean defective = false;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private LocationModel location;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeviceFileModel> files = new ArrayList<>();
}