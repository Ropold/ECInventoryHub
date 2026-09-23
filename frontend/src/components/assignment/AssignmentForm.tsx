import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import axios from "axios";
import type {DeviceModel} from "../models/DeviceModel.ts";
import type {EmployeeModel} from "../models/EmployeeModel.ts";
import type {AssignmentFileModel} from "../models/AssignmentFileModel.ts";
import "../styles/FormStyles.css";

type AssignmentFormProps = {
    language: string;
    backNavigationPath: string;
    handleSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
    deviceId: string;
    setDeviceId: React.Dispatch<React.SetStateAction<string>>;
    employeeId: string;
    setEmployeeId: React.Dispatch<React.SetStateAction<string>>;
    handedOutById: string | undefined;
    setHandedOutById: React.Dispatch<React.SetStateAction<string | undefined>>;
    assignedDate: string;
    setAssignedDate: React.Dispatch<React.SetStateAction<string>>;
    returnedDate: string | undefined;
    setReturnedDate: React.Dispatch<React.SetStateAction<string | undefined>>;
    conditionOut: string | undefined;
    setConditionOut: React.Dispatch<React.SetStateAction<string | undefined>>;
    conditionIn: string | undefined;
    setConditionIn: React.Dispatch<React.SetStateAction<string | undefined>>;
    notes: string | undefined;
    setNotes: React.Dispatch<React.SetStateAction<string | undefined>>;
    copyHandedToEmployee: boolean;
    setCopyHandedToEmployee: React.Dispatch<React.SetStateAction<boolean>>;
    copyFiledInPersonnelFile: boolean;
    setCopyFiledInPersonnelFile: React.Dispatch<React.SetStateAction<boolean>>;
    newFiles: File[];
    setNewFiles: React.Dispatch<React.SetStateAction<File[]>>;
    existingFiles?: AssignmentFileModel[];
    setExistingFiles?: React.Dispatch<React.SetStateAction<AssignmentFileModel[]>>;
}

function getDeviceLabel(device: DeviceModel): string {
    const name = [device.manufacturer, device.modelName].filter(Boolean).join(" ");
    const identifier = device.inventoryNumber ?? device.serialNumber;
    return identifier ? `${name} (${identifier})` : name;
}

export default function AssignmentForm(props: Readonly<AssignmentFormProps>) {

    const {
        backNavigationPath,
        handleSubmit,
        deviceId,
        setDeviceId,
        employeeId,
        setEmployeeId,
        handedOutById,
        setHandedOutById,
        assignedDate,
        setAssignedDate,
        returnedDate,
        setReturnedDate,
        conditionOut,
        setConditionOut,
        conditionIn,
        setConditionIn,
        notes,
        setNotes,
        copyHandedToEmployee,
        setCopyHandedToEmployee,
        copyFiledInPersonnelFile,
        setCopyFiledInPersonnelFile,
        newFiles,
        setNewFiles,
        existingFiles,
        setExistingFiles
    } = props;

    const [devices, setDevices] = useState<DeviceModel[]>([]);
    const [employees, setEmployees] = useState<EmployeeModel[]>([]);

    const navigate = useNavigate();
    const isEditMode = backNavigationPath.includes('/assignments/') && backNavigationPath !== '/assignments';

    useEffect(() => {
        axios
            .get("/api/devices")
            .then((response) => setDevices(response.data))
            .catch((error) => console.error("Error fetching devices", error));

        axios
            .get("/api/employees")
            .then((response) => setEmployees(response.data))
            .catch((error) => console.error("Error fetching employees", error));
    }, []);

    function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
        if (!e.target.files) return;
        setNewFiles(Array.from(e.target.files));
    }

    // Vorhandene und neue Dateien in einer Liste, jeweils mit eigenem Entfernen-Handler
    const fileItems = [
        ...(existingFiles ?? []).map((file) => ({
            key: file.id,
            label: <a href={file.fileUrl} target="_blank" rel="noopener noreferrer">{file.fileUrl}</a>,
            onRemove: () => setExistingFiles?.((prevFiles) => prevFiles.filter((f) => f.id !== file.id)),
        })),
        ...newFiles.map((file) => ({
            key: file.name,
            label: <span>{file.name}</span>,
            onRemove: () => setNewFiles(newFiles.filter((f) => f.name !== file.name)),
        })),
    ];

    return (
        <div>
            <h2>{isEditMode ? "Edit Assignment" : "Add Assignment"}</h2>

            <form onSubmit={handleSubmit}>
                <div className="edit-form">
                    {/* Device */}
                    <label>
                        <span>Device:</span>
                        <select
                            className="input-small"
                            value={deviceId}
                            onChange={(e) => setDeviceId(e.target.value)}
                            required
                        >
                            <option value="">-- select device --</option>
                            {devices.map((device) => (
                                <option key={device.id} value={device.id}>{getDeviceLabel(device)}</option>
                            ))}
                        </select>
                    </label>

                    {/* Employee */}
                    <label>
                        <span>Employee:</span>
                        <select
                            className="input-small"
                            value={employeeId}
                            onChange={(e) => setEmployeeId(e.target.value)}
                            required
                        >
                            <option value="">-- select employee --</option>
                            {employees.map((employee) => (
                                <option key={employee.id} value={employee.id}>{employee.name}</option>
                            ))}
                        </select>
                    </label>

                    {/* Handed Out By */}
                    <label>
                        <span>Handed Out By:</span>
                        <select
                            className="input-small"
                            value={handedOutById ?? ""}
                            onChange={(e) => setHandedOutById(e.target.value || undefined)}
                        >
                            <option value="">-- none --</option>
                            {employees.map((employee) => (
                                <option key={employee.id} value={employee.id}>{employee.name}</option>
                            ))}
                        </select>
                    </label>

                    {/* Assigned Date */}
                    <label>
                        <span>Assigned Date:</span>
                        <input
                            className="input-small"
                            type="date"
                            value={assignedDate}
                            onChange={(e) => setAssignedDate(e.target.value)}
                            required
                        />
                    </label>

                    {/* Returned Date */}
                    <label>
                        <span>Returned Date:</span>
                        <input
                            className="input-small"
                            type="date"
                            value={returnedDate ?? ""}
                            onChange={(e) => setReturnedDate(e.target.value || undefined)}
                        />
                    </label>

                    {/* Condition Out */}
                    <label>
                        <span>Condition Out:</span>
                        <textarea
                            className="input-small"
                            value={conditionOut ?? ""}
                            onChange={(e) => setConditionOut(e.target.value || undefined)}
                        />
                    </label>

                    {/* Condition In */}
                    <label>
                        <span>Condition In:</span>
                        <textarea
                            className="input-small"
                            value={conditionIn ?? ""}
                            onChange={(e) => setConditionIn(e.target.value || undefined)}
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

                    {/* Copy Handed To Employee */}
                    <label>
                        <span>Copy Handed To Employee:</span>
                        <input
                            type="checkbox"
                            checked={copyHandedToEmployee}
                            onChange={(e) => setCopyHandedToEmployee(e.target.checked)}
                        />
                    </label>

                    {/* Copy Filed In Personnel File */}
                    <label>
                        <span>Copy Filed In Personnel File:</span>
                        <input
                            type="checkbox"
                            checked={copyFiledInPersonnelFile}
                            onChange={(e) => setCopyFiledInPersonnelFile(e.target.checked)}
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

                {fileItems.length > 0 && (
                    <ul className="assignment-file-list">
                        {fileItems.map((item) => (
                            <li key={item.key}>
                                {item.label}
                                <button type="button" className="button-blue margin-left-20" onClick={item.onRemove}>
                                    remove file
                                </button>
                            </li>
                        ))}
                    </ul>
                )}

                <button type="submit" className="button-blue margin-top-50">
                    {isEditMode ? "Update Assignment" : "Add Assignment"}
                </button>
                <button type="button" className="button-blue margin-left-20" onClick={() => navigate(backNavigationPath)}>
                    back
                </button>
            </form>
        </div>
    )
}
