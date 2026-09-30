import {translatedInfo} from "../utils/TranslatedInfo.ts";
import type {AssignmentModel} from "../models/AssignmentModel.ts";
import {useNavigate, useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import axios from "axios";
import {formatDate, handleRequestError} from "../utils/ComponentsFunctions.tsx";
import "../styles/Details.css";
import NoPermissionPopup from "../NoPermissionPopup.tsx";

type AssignmentDetailsProps = {
    language: string;
    role: string;
    handleAssignmentUpdate: (updatedAssignment: AssignmentModel) => void;
    handleAssignmentDelete: (deletedAssignmentId: string) => void;
}

export default function AssignmentDetails(props: Readonly<AssignmentDetailsProps>) {
    const [assignment, setAssignment] = useState<AssignmentModel | null>(null);
    const {id} = useParams<{id: string}>();
    const navigate = useNavigate();
    const [showPopup, setShowPopup] = useState(false);
    const [deleteError, setDeleteError] = useState<string | null>(null);
    const [showNoPermission, setShowNoPermission] = useState<boolean>(false);

    // Viewer dürfen weder bearbeiten noch löschen
    function withPermission(action: () => void) {
        if (props.role === "VIEWER") setShowNoPermission(true);
        else action();
    }

    useEffect(() => {
        if (!id) return;
        axios
            .get(`/api/assignments/${id}`)
            .then((response) => setAssignment(response.data))
            .catch((error) => console.error("Error fetching assignment details", error));
    }, [id]);

    function handleConfirmDelete() {
        if (!assignment) return;

        axios
            .delete(`/api/assignments/${id}`)
            .then(() => {
                console.log("Successfully deleted assignment");
                props.handleAssignmentDelete(assignment.id);
                setShowPopup(false);
                navigate("/assignments");
            })
            .catch((error) => {
                console.error("Error deleting assignment", error);
                handleRequestError(error,
                    "You must be logged in as User/Admin to delete an assignment.",
                    "Error deleting assignment. Please try again.",
                    setDeleteError);
            })
    }

    function handleCancel() {
        setShowPopup(false);
        setDeleteError(null);
    }

    const deviceName = assignment
        ? [assignment.device.manufacturer, assignment.device.modelName].filter(Boolean).join(" ")
        : "";

    return (
        <div>
            <h2>{translatedInfo["Assignment Details"][props.language]}</h2>
            {assignment ? (
                <div className="details-container">
                    <h3>{translatedInfo["Device"][props.language]}</h3>
                    <p><strong>{translatedInfo["Device"][props.language]}:</strong> {deviceName}</p>
                    <p><strong>{translatedInfo["Type"][props.language]}:</strong> {assignment.device.type}</p>
                    {assignment.device.inventoryNumber &&
                        <p><strong>{translatedInfo["Inventory Number"][props.language]}:</strong> {assignment.device.inventoryNumber}</p>}
                    {assignment.device.hostname &&
                        <p><strong>{translatedInfo["Hostname"][props.language]}:</strong> {assignment.device.hostname}</p>}
                    {assignment.device.serialNumber &&
                        <p><strong>{translatedInfo["Serial Number"][props.language]}:</strong> {assignment.device.serialNumber}</p>}

                    <h3>{translatedInfo["Employee"][props.language]}</h3>
                    <p><strong>{translatedInfo["Employee"][props.language]}:</strong> {assignment.employee.name}</p>
                    <p><strong>{translatedInfo["Department"][props.language]}:</strong> {assignment.employee.department}</p>
                    {assignment.handedOutBy &&
                        <p><strong>{translatedInfo["Handed Out By"][props.language]}:</strong> {assignment.handedOutBy.name}</p>}

                    <h3>{translatedInfo["Period"][props.language]}</h3>
                    <p><strong>{translatedInfo["Assigned Date"][props.language]}:</strong> {formatDate(assignment.assignedDate)}</p>
                    <p><strong>{translatedInfo["Returned Date"][props.language]}:</strong> {formatDate(assignment.returnedDate ?? undefined)}</p>

                    <h3>{translatedInfo["Condition"][props.language]}</h3>
                    {assignment.conditionOut && <p><strong>{translatedInfo["Condition Out"][props.language]}:</strong> {assignment.conditionOut}</p>}
                    {assignment.conditionIn && <p><strong>{translatedInfo["Condition In"][props.language]}:</strong> {assignment.conditionIn}</p>}
                    <p><strong>{translatedInfo["Copy Handed To Employee"][props.language]}:</strong> {assignment.copyHandedToEmployee ? translatedInfo["Yes"][props.language] : translatedInfo["No"][props.language]}</p>
                    <p><strong>{translatedInfo["Copy Filed In Personnel File"][props.language]}:</strong> {assignment.copyFiledInPersonnelFile ? translatedInfo["Yes"][props.language] : translatedInfo["No"][props.language]}</p>

                    {assignment.notes && (
                        <>
                            <h3>{translatedInfo["Notes"][props.language]}</h3>
                            <p>{assignment.notes}</p>
                        </>
                    )}

                    {assignment.files.length > 0 && (
                        <>
                            <h3>{translatedInfo["Files"][props.language]}</h3>
                            <ul className="assignment-file-list">
                                {assignment.files.map((file) => (
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
                    <p><strong>{translatedInfo["ID"][props.language]}:</strong> {assignment.id}</p>

                    <div className="details-buttons">
                        <button className="button-blue" onClick={() => withPermission(() => navigate(`/assignments/${id}/edit`))}>{translatedInfo["Edit"][props.language]}</button>
                        <button className="button-delete" onClick={() => withPermission(() => setShowPopup(true))}>{translatedInfo["Delete"][props.language]}</button>
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
                                <p>{translatedInfo["Delete assignment confirmation"][props.language].replace("{name}", assignment.employee.name)}</p>
                                {deleteError && (
                                    <div className="popup-error">
                                        <p>{deleteError}</p>
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
