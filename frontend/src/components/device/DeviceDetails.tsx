import {translatedInfo} from "../utils/TranslatedInfo.ts";
import type {DeviceModel} from "../models/DeviceModel.ts";
import {useNavigate, useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import axios from "axios";
import {formatDate, handleRequestError, renderBlockingList} from "../utils/ComponentsFunctions.tsx";
import "../styles/Details.css";
import NoPermissionPopup from "../NoPermissionPopup.tsx";
import type {AssignmentModel} from "../models/AssignmentModel.ts";
import EmployeeCard from "../employee/EmployeeCard.tsx";
import AssignmentCard from "../assignment/AssignmentCard.tsx";
import {deviceStatusLabelKeys, deviceTypeLabelKeys} from "../utils/DeviceFilters.ts";

type DeviceDetailsProps = {
    language: string;
    role: string;
    handleDeviceUpdate: (updatedDevice: DeviceModel) => void;
    handleDeviceDelete: (deletedDeviceId: string) => void;
    assignments: AssignmentModel[];
}

export default function DeviceDetails(props: Readonly<DeviceDetailsProps>) {
    const [device, setDevice] = useState<DeviceModel | null>(null);
    const {id} = useParams<{id: string}>();
    const navigate = useNavigate();
    const [showPopup, setShowPopup] = useState(false);
    const [deleteError, setDeleteError] = useState<string | null>(null);
    const [blockingAssignments, setBlockingAssignments] = useState<string[]>([]);
    const [showNoPermission, setShowNoPermission] = useState<boolean>(false);

    // Alle Zuweisungen dieses Geräts, neueste zuerst – die aktuelle ist die ohne Rückgabedatum
    const deviceAssignments = props.assignments
        .filter((assignment) => assignment.device.id === id)
        .sort((a, b) => b.assignedDate.localeCompare(a.assignedDate));
    const currentAssignment = deviceAssignments.find((assignment) => !assignment.returnedDate);

    function handleEditClick() {
        if (props.role === "VIEWER") {
            setShowNoPermission(true);
            return;
        }
        navigate(`/devices/${id}/edit`);
    }

    function handleDeleteClick() {
        if (props.role === "VIEWER") {
            setShowNoPermission(true);
            return;
        }
        setShowPopup(true);
    }

    useEffect(() => {
        if (!id) return;
        axios
            .get(`/api/devices/${id}`)
            .then((response) => setDevice(response.data))
            .catch((error) => console.error("Error fetching device details", error));
    }, [id]);

    function handleConfirmDelete() {
        if (!device) return;

        axios
            .delete(`/api/devices/${id}`)
            .then(() => {
                console.log("Successfully deleted device");
                props.handleDeviceDelete(device.id);
                setShowPopup(false);
                navigate("/devices");
            })
            .catch((error) => {
                console.error("Error deleting device", error);
                handleRequestError(error,
                    "You must be logged in as User/Admin to delete a device.",
                    "Error deleting device. Please try again.",
                    setDeleteError, setBlockingAssignments);
            })
    }

    function handleCancel() {
        setShowPopup(false);
        setDeleteError(null);
        setBlockingAssignments([]);
    }

    const deviceName = device
        ? [device.manufacturer, device.modelName].filter(Boolean).join(" ")
        : "";

    return (
        <div>
            <h2>{translatedInfo["Device Details"][props.language]}</h2>
            {device ? (
                <div className="details-container">
                    <h3>{translatedInfo["Basic Information"][props.language]}</h3>
                    <p><strong>{translatedInfo["Device"][props.language]}:</strong> {deviceName}</p>
                    <p><strong>{translatedInfo["Type"][props.language]}:</strong> {translatedInfo[deviceTypeLabelKeys[device.type]][props.language]}</p>
                    <p><strong>{translatedInfo["Status"][props.language]}:</strong> {translatedInfo[deviceStatusLabelKeys[device.status]][props.language]}</p>
                    <p><strong>{translatedInfo["Defective"][props.language]}:</strong> {device.defective ? translatedInfo["Yes"][props.language] : translatedInfo["No"][props.language]}</p>
                    <h3>{translatedInfo["Identification"][props.language]}</h3>
                    {device.inventoryNumber && <p><strong>{translatedInfo["Inventory Number"][props.language]}:</strong> {device.inventoryNumber}</p>}
                    {device.hostname && <p><strong>{translatedInfo["Hostname"][props.language]}:</strong> {device.hostname}</p>}
                    {device.serialNumber && <p><strong>{translatedInfo["Serial Number"][props.language]}:</strong> {device.serialNumber}</p>}
                    <p><strong>{translatedInfo["Purchase Date"][props.language]}:</strong> {formatDate(device.purchaseDate ?? undefined)}</p>

                    {currentAssignment && (
                        <div className="details-card-container">
                            <div className="details-card-labeled">
                                <h3>{translatedInfo["Employee"][props.language]}</h3>
                                <EmployeeCard employee={currentAssignment.employee} language={props.language} />
                            </div>
                            {currentAssignment.handedOutBy && (
                                <div className="details-card-labeled">
                                    <h3>{translatedInfo["Handed Out By"][props.language]}</h3>
                                    <EmployeeCard employee={currentAssignment.handedOutBy} language={props.language} />
                                </div>
                            )}
                        </div>
                    )}

                    {deviceAssignments.length > 0 && (
                        <>
                            <h3>{translatedInfo["Assignment History"][props.language]}</h3>
                            <div className="assignment-card-container">
                                {deviceAssignments.map((assignment) => (
                                    <AssignmentCard key={assignment.id} assignment={assignment} language={props.language} hideDevice />
                                ))}
                            </div>
                        </>
                    )}

                    {device.location && (
                        <>
                            <h3>{translatedInfo["Device Location"][props.language]}</h3>
                            <p><strong>{translatedInfo["Name"][props.language]}:</strong> {device.location.name}</p>
                            {device.location.address && <p><strong>{translatedInfo["Address"][props.language]}:</strong> {device.location.address}</p>}
                            {device.location.phone && <p><strong>{translatedInfo["Phone"][props.language]}:</strong> {device.location.phone}</p>}
                            {device.location.email && <p><strong>{translatedInfo["Email"][props.language]}:</strong> {device.location.email}</p>}
                        </>
                    )}

                    {device.notes && (
                        <>
                            <h3>{translatedInfo["Notes"][props.language]}</h3>
                            <p>{device.notes}</p>
                        </>
                    )}

                    {device.files.length > 0 && (
                        <>
                            <h3>{translatedInfo["Files"][props.language]}</h3>
                            <ul className="device-file-list">
                                {device.files.map((file) => (
                                    <li key={file.id}>
                                        <a href={file.fileUrl} target="_blank" rel="noopener noreferrer">
                                            {file.fileType ?? file.fileUrl}
                                        </a>
                                    </li>
                                ))}
                            </ul>
                        </>
                    )}

                    <h3>{translatedInfo["Metadata"][props.language]}</h3>
                    <p><strong>{translatedInfo["ID"][props.language]}:</strong> {device.id}</p>

                    <div className="details-buttons">
                        <button className="button-blue" onClick={handleEditClick}>{translatedInfo["Edit"][props.language]}</button>
                        <button className="button-delete" onClick={handleDeleteClick}>{translatedInfo["Delete"][props.language]}</button>
                    </div>

                    {showNoPermission && (
                        <NoPermissionPopup
                            language={props.language}
                            onClose={() => setShowNoPermission(false)}
                        />
                    )}

                    {showPopup && (
                        <div className="popup-overlay">
                            <div className="popup-content">
                                <h3>{translatedInfo["Confirm Deletion"][props.language]}</h3>
                                <p>{translatedInfo["Delete confirmation"][props.language].replace("{name}", deviceName)}</p>
                                {deleteError && (
                                    <div className="popup-error">
                                        <p>{deleteError}</p>
                                        {renderBlockingList(blockingAssignments)}
                                    </div>
                                )}
                                <div className="popup-actions">
                                    <button onClick={handleConfirmDelete} className="popup-confirm">{translatedInfo["Yes, Delete"][props.language]}</button>
                                    <button onClick={handleCancel} className="popup-cancel">{translatedInfo["Cancel"][props.language]}</button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            ) : (
                <p>{translatedInfo["Loading..."][props.language]}</p>
            )}
        </div>
    )
}
