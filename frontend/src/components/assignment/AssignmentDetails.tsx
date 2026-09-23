import {translatedInfo} from "../utils/TranslatedInfo.ts";
import type {AssignmentModel} from "../models/AssignmentModel.ts";
import {useNavigate, useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import axios from "axios";
import {formatDate} from "../utils/ComponentsFunctions.tsx";
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

                if (error.response?.status === 401) {
                    setDeleteError("You must be logged in as User/Admin to delete an assignment.");
                } else {
                    setDeleteError("Error deleting assignment. Please try again.");
                }
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
            <h2>Assignment Details</h2>
            {assignment ? (
                <div className="details-container">
                    <h3>Device</h3>
                    <p><strong>Device:</strong> {deviceName}</p>
                    <p><strong>Type:</strong> {assignment.device.type}</p>
                    {assignment.device.inventoryNumber &&
                        <p><strong>Inventory Number:</strong> {assignment.device.inventoryNumber}</p>}
                    {assignment.device.serialNumber &&
                        <p><strong>Serial Number:</strong> {assignment.device.serialNumber}</p>}

                    <h3>Employee</h3>
                    <p><strong>Employee:</strong> {assignment.employee.name}</p>
                    <p><strong>Department:</strong> {assignment.employee.department}</p>
                    {assignment.handedOutBy &&
                        <p><strong>Handed Out By:</strong> {assignment.handedOutBy.name}</p>}

                    <h3>Period</h3>
                    <p><strong>Assigned Date:</strong> {formatDate(assignment.assignedDate)}</p>
                    <p><strong>Returned Date:</strong> {formatDate(assignment.returnedDate ?? undefined)}</p>

                    <h3>Condition</h3>
                    {assignment.conditionOut && <p><strong>Condition Out:</strong> {assignment.conditionOut}</p>}
                    {assignment.conditionIn && <p><strong>Condition In:</strong> {assignment.conditionIn}</p>}
                    <p><strong>Copy Handed To Employee:</strong> {assignment.copyHandedToEmployee ? 'Yes' : 'No'}</p>
                    <p><strong>Copy Filed In Personnel File:</strong> {assignment.copyFiledInPersonnelFile ? 'Yes' : 'No'}</p>

                    {assignment.notes && (
                        <>
                            <h3>Notes</h3>
                            <p>{assignment.notes}</p>
                        </>
                    )}

                    {assignment.files.length > 0 && (
                        <>
                            <h3>Files</h3>
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

                    <h3>Metadata</h3>
                    <p><strong>ID:</strong> {assignment.id}</p>

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
                                <h3>Confirm Deletion</h3>
                                <p>Are you sure you want to delete the assignment
                                    for {assignment.employee.name}?</p>
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
                <p>Loading...</p>
            )}
        </div>
    )
}
