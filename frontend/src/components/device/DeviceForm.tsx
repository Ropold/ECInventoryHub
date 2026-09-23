import {translatedInfo} from "../utils/TranslatedInfo.ts";
import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import axios from "axios";
import type {DeviceStatus, DeviceType} from "../models/DeviceModel.ts";
import type {LocationModel} from "../models/LocationModel.ts";
import type {DeviceFileModel} from "../models/DeviceFileModel.ts";
import "../styles/FormStyles.css";

type DeviceFormProps = {
    language: string;
    backNavigationPath: string;
    handleSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
    type: DeviceType;
    setType: React.Dispatch<React.SetStateAction<DeviceType>>;
    manufacturer: string | undefined;
    setManufacturer: React.Dispatch<React.SetStateAction<string | undefined>>;
    modelName: string | undefined;
    setModelName: React.Dispatch<React.SetStateAction<string | undefined>>;
    serialNumber: string | undefined;
    setSerialNumber: React.Dispatch<React.SetStateAction<string | undefined>>;
    inventoryNumber: string | undefined;
    setInventoryNumber: React.Dispatch<React.SetStateAction<string | undefined>>;
    purchaseDate: string | undefined;
    setPurchaseDate: React.Dispatch<React.SetStateAction<string | undefined>>;
    status: DeviceStatus;
    setStatus: React.Dispatch<React.SetStateAction<DeviceStatus>>;
    defective: boolean;
    setDefective: React.Dispatch<React.SetStateAction<boolean>>;
    locationId: string | undefined;
    setLocationId: React.Dispatch<React.SetStateAction<string | undefined>>;
    notes: string | undefined;
    setNotes: React.Dispatch<React.SetStateAction<string | undefined>>;
    newFiles: File[];
    setNewFiles: React.Dispatch<React.SetStateAction<File[]>>;
    existingFiles?: DeviceFileModel[];
    setExistingFiles?: React.Dispatch<React.SetStateAction<DeviceFileModel[]>>;
}

export default function DeviceForm(props: Readonly<DeviceFormProps>) {

    const {
        backNavigationPath,
        handleSubmit,
        type,
        setType,
        manufacturer,
        setManufacturer,
        modelName,
        setModelName,
        serialNumber,
        setSerialNumber,
        inventoryNumber,
        setInventoryNumber,
        purchaseDate,
        setPurchaseDate,
        status,
        setStatus,
        defective,
        setDefective,
        locationId,
        setLocationId,
        notes,
        setNotes,
        newFiles,
        setNewFiles,
        existingFiles,
        setExistingFiles
    } = props;

    const [locations, setLocations] = useState<LocationModel[]>([]);

    const navigate = useNavigate();
    const isEditMode = backNavigationPath.includes('/devices/') && backNavigationPath !== '/devices';

    useEffect(() => {
        axios
            .get("/api/locations")
            .then((response) => setLocations(response.data))
            .catch((error) => console.error("Error fetching locations", error));
    }, []);

    function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
        if (!e.target.files) return;
        setNewFiles(Array.from(e.target.files));
    }

    function handleRemoveNewFile(fileName: string) {
        setNewFiles(newFiles.filter((file) => file.name !== fileName));
    }

    function handleRemoveExistingFile(fileId: string) {
        if (!setExistingFiles) return;
        setExistingFiles((prevFiles) => prevFiles.filter((file) => file.id !== fileId));
    }

    return (
        <div>
            <h2>{isEditMode ? "Edit Device" : "Add Device"}</h2>

            <form onSubmit={handleSubmit}>
                <div className="edit-form">
                    {/* Type */}
                    <label>
                        <span>Type:</span>
                        <select
                            className="input-small"
                            value={type}
                            onChange={(e) => setType(e.target.value as DeviceType)}
                            required
                        >
                            <option value="LAPTOP">Laptop</option>
                            <option value="PHONE">Phone</option>
                            <option value="TABLET">Tablet</option>
                            <option value="MONITOR">Monitor</option>
                            <option value="ACCESSORY">Accessory</option>
                            <option value="OTHER">Other</option>
                        </select>
                    </label>

                    {/* Status */}
                    <label>
                        <span>Status:</span>
                        <select
                            className="input-small"
                            value={status}
                            onChange={(e) => setStatus(e.target.value as DeviceStatus)}
                            required
                        >
                            <option value="AVAILABLE">Available</option>
                            <option value="ASSIGNED">Assigned</option>
                            <option value="IN_REPAIR">In Repair</option>
                            <option value="RETIRED">Retired</option>
                        </select>
                    </label>

                    {/* Manufacturer */}
                    <label>
                        <span>Manufacturer:</span>
                        <input
                            className="input-small"
                            type="text"
                            value={manufacturer ?? ""}
                            onChange={(e) => setManufacturer(e.target.value || undefined)}
                        />
                    </label>

                    {/* Model Name */}
                    <label>
                        <span>Model Name:</span>
                        <input
                            className="input-small"
                            type="text"
                            value={modelName ?? ""}
                            onChange={(e) => setModelName(e.target.value || undefined)}
                        />
                    </label>

                    {/* Serial Number */}
                    <label>
                        <span>Serial Number:</span>
                        <input
                            className="input-small"
                            type="text"
                            value={serialNumber ?? ""}
                            onChange={(e) => setSerialNumber(e.target.value || undefined)}
                        />
                    </label>

                    {/* Inventory Number */}
                    <label>
                        <span>Inventory Number:</span>
                        <input
                            className="input-small"
                            type="text"
                            value={inventoryNumber ?? ""}
                            onChange={(e) => setInventoryNumber(e.target.value || undefined)}
                        />
                    </label>

                    {/* Purchase Date */}
                    <label>
                        <span>Purchase Date:</span>
                        <input
                            className="input-small"
                            type="date"
                            value={purchaseDate ?? ""}
                            onChange={(e) => setPurchaseDate(e.target.value || undefined)}
                        />
                    </label>

                    {/* Location */}
                    <label>
                        <span>Location:</span>
                        <select
                            className="input-small"
                            value={locationId ?? ""}
                            onChange={(e) => setLocationId(e.target.value || undefined)}
                        >
                            <option value="">-- none --</option>
                            {locations.map((location) => (
                                <option key={location.id} value={location.id}>{location.name}</option>
                            ))}
                        </select>
                    </label>

                    {/* Defective */}
                    <label>
                        <span>Defective:</span>
                        <input
                            type="checkbox"
                            checked={defective}
                            onChange={(e) => setDefective(e.target.checked)}
                        />
                    </label>

                    {/* Notes */}
                    <label>
                        <span>Notes:</span>
                        <textarea
                            className="input-small"
                            value={notes ?? ""}
                            onChange={(e) => setNotes(e.target.value || undefined)}
                        />
                    </label>

                    {/* Files */}
                    <label>
                        <span>Files:</span>
                        <input
                            type="file"
                            multiple
                            onChange={handleFileChange}
                        />
                    </label>
                </div>

                {existingFiles && existingFiles.length > 0 && (
                    <ul className="device-file-list">
                        {existingFiles.map((file) => (
                            <li key={file.id}>
                                <a href={file.fileUrl} target="_blank" rel="noopener noreferrer">{file.fileUrl}</a>
                                <button
                                    type="button"
                                    className="button-blue margin-left-20"
                                    onClick={() => handleRemoveExistingFile(file.id)}
                                >
                                    {translatedInfo["remove file"][props.language]}
                                </button>
                            </li>
                        ))}
                    </ul>
                )}

                {newFiles.length > 0 && (
                    <ul className="device-file-list">
                        {newFiles.map((file) => (
                            <li key={file.name}>
                                <span>{file.name}</span>
                                <button
                                    type="button"
                                    className="button-blue margin-left-20"
                                    onClick={() => handleRemoveNewFile(file.name)}
                                >
                                    {translatedInfo["remove file"][props.language]}
                                </button>
                            </li>
                        ))}
                    </ul>
                )}

                <button type="submit" className="button-blue margin-top-50">
                    {isEditMode ? translatedInfo["Update Device"][props.language] : translatedInfo["Add Device"][props.language]}
                </button>
                <button type="button" className="button-blue margin-left-20" onClick={() => navigate(backNavigationPath)}>
                    {translatedInfo["back"][props.language]}
                </button>
            </form>
        </div>
    )
}
