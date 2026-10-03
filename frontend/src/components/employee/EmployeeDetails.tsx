import {translatedInfo} from "../utils/TranslatedInfo.ts";
import type {EmployeeModel} from "../models/EmployeeModel.ts";
import {useNavigate, useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import axios from "axios";
import {handleRequestError, renderBlockingList} from "../utils/ComponentsFunctions.tsx";
import "../styles/Details.css";
import NoPermissionPopup from "../NoPermissionPopup.tsx";
import type {AssignmentModel} from "../models/AssignmentModel.ts";
import AssignmentCard from "../assignment/AssignmentCard.tsx";

type EmployeeDetailsProps = {
    language: string;
    role: string;
    handleEmployeeUpdate: (updatedEmployee: EmployeeModel) => void;
    handleEmployeeDelete: (deletedEmployeeId: string) => void;
    assignments: AssignmentModel[];
}

export default function EmployeeDetails(props: Readonly<EmployeeDetailsProps>) {
    const [employee, setEmployee] = useState<EmployeeModel | null>(null);
    const {id} = useParams<{id: string}>();
    const navigate = useNavigate();
    const [showPopup, setShowPopup] = useState(false);
    const [deleteError, setDeleteError] = useState<string | null>(null);
    const [blockingAssignments, setBlockingAssignments] = useState<string[]>([]);
    const [showNoPermission, setShowNoPermission] = useState<boolean>(false);

    const byNewestFirst = (a: AssignmentModel, b: AssignmentModel) => b.assignedDate.localeCompare(a.assignedDate);
    // Geräte, die der Mitarbeiter selbst hat bzw. hatte
    const employeeAssignments = props.assignments
        .filter((assignment) => assignment.employee.id === id)
        .sort(byNewestFirst);
    // Zuweisungen, die der Mitarbeiter an andere ausgegeben hat
    const handedOutAssignments = props.assignments
        .filter((assignment) => assignment.handedOutBy?.id === id)
        .sort(byNewestFirst);

    function handleEditClick() {
        if (props.role === "VIEWER") {
            setShowNoPermission(true);
            return;
        }
        navigate(`/employees/${id}/edit`);
    }

    function handleDeleteClick() {
        if (props.role === "VIEWER") {
            setShowNoPermission(true);
            return;
        }
        setShowPopup(true);
    }

    useEffect(() => {
        if(!id) return;
        axios
            .get(`/api/employees/${id}`)
            .then((response) => setEmployee(response.data))
            .catch((error) => console.error("Error fetching employee details", error));
    }, [id]);

    function handleConfirmDelete(){
        if(!employee) return;

        axios
            .delete(`/api/employees/${id}`)
            .then(() => {
                console.log("Successfully deleted employee");
                props.handleEmployeeDelete(employee.id);
                setShowPopup(false);
                navigate("/employees");
            })
            .catch((error) => {
                console.error("Error deleting employee", error);
                handleRequestError(error,
                    "You must be logged in as User/Admin to delete an employee.",
                    "Error deleting employee. Please try again.",
                    setDeleteError, setBlockingAssignments);
            })
    }

    function handleForceDelete(){
        if(!employee) return;

        axios
            .delete(`/api/employees/${id}/force`)
            .then(() => {
                console.log("Successfully deleted employee and all assignments");
                props.handleEmployeeDelete(employee.id);
                setShowPopup(false);
                navigate("/employees");
            })
            .catch((error) => {
                console.error("Error force deleting employee", error);

                if (error.response?.status === 401) {
                    setDeleteError("You must be logged in as Admin to delete an employee with assignments.");
                } else if (error.response?.status === 403) {
                    setDeleteError("You must be an Admin to delete an employee with assignments.");
                } else {
                    setDeleteError("Error deleting employee. Please try again.");
                }
            })
    }

    function handleCancel(){
        setShowPopup(false);
        setDeleteError(null);
        setBlockingAssignments([]);
    }


    return(
        <div>
            <h2>{translatedInfo["Employee Details"][props.language]}</h2>
            {employee ? (
                <div className="details-container">
                    {employee.imageUrl && (
                        <div className="details-img-container">
                            <img src={employee.imageUrl} alt={employee.name} className="details-image"/>
                        </div>
                    )}

                    {employeeAssignments.length > 0 && (
                        <>
                            <h3>{translatedInfo["Assignments"][props.language]}</h3>
                            <div className="assignment-card-container">
                                {employeeAssignments.map((assignment) => (
                                    <AssignmentCard key={assignment.id} assignment={assignment} language={props.language} hideEmployee />
                                ))}
                            </div>
                        </>
                    )}

                    {handedOutAssignments.length > 0 && (
                        <details className="details-collapsible">
                            <summary>
                                {translatedInfo["Handed out assignments"][props.language]} ({handedOutAssignments.length})
                            </summary>
                            <div className="assignment-card-container">
                                {handedOutAssignments.map((assignment) => (
                                    <AssignmentCard key={assignment.id} assignment={assignment} language={props.language} />
                                ))}
                            </div>
                        </details>
                    )}

                    <h3>{translatedInfo["Basic Information"][props.language]}</h3>
                    <p><strong>{translatedInfo["Name"][props.language]}:</strong> {employee.name}</p>
                    {employee.personnelNumber && <p><strong>{translatedInfo["Personnel Number"][props.language]}:</strong> {employee.personnelNumber}</p>}
                    <p><strong>{translatedInfo["Department"][props.language]}:</strong> {employee.department}</p>
                    <p><strong>{translatedInfo["Status"][props.language]}:</strong> {employee.active ? translatedInfo["Active"][props.language] : translatedInfo["Inactive"][props.language]}</p>

                    <h3>{translatedInfo["Contact Information"][props.language]}</h3>
                    {employee.email && <p><strong>{translatedInfo["Email"][props.language]}:</strong> {employee.email}</p>}
                    {employee.phone && <p><strong>{translatedInfo["Phone"][props.language]}:</strong> {employee.phone}</p>}
                    {employee.address && <p><strong>{translatedInfo["Address"][props.language]}:</strong> {employee.address}</p>}

                    {employee.notes && (
                        <>
                            <h3>{translatedInfo["Notes"][props.language]}</h3>
                            <p>{employee.notes}</p>
                        </>
                    )}

                    <h3>{translatedInfo["Metadata"][props.language]}</h3>
                    <p><strong>{translatedInfo["ID"][props.language]}:</strong> {employee.id}</p>

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
                                <p>{translatedInfo["Delete confirmation"][props.language].replace("{name}", employee.name)}</p>
                                {deleteError && (
                                    <div className="popup-error">
                                        <p>{deleteError}</p>
                                        {renderBlockingList(blockingAssignments)}
                                    </div>
                                )}
                                <div className="popup-actions">
                                    <button onClick={handleConfirmDelete} className="popup-confirm">{translatedInfo["Yes, Delete"][props.language]}</button>
                                    {blockingAssignments.length > 0 && props.role === "ADMIN" && (
                                        <button onClick={handleForceDelete} className="popup-confirm">{translatedInfo["Delete All"][props.language]}</button>
                                    )}
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