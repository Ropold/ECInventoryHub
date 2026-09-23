import {translatedInfo} from "../utils/TranslatedInfo.ts";
import type {EmployeeModel} from "../models/EmployeeModel.ts";
import {useNavigate, useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import axios from "axios";
import "../styles/Details.css";
import NoPermissionPopup from "../NoPermissionPopup.tsx";

type EmployeeDetailsProps = {
    language: string;
    role: string;
    handleEmployeeUpdate: (updatedEmployee: EmployeeModel) => void;
    handleEmployeeDelete: (deletedEmployeeId: string) => void;
}

export default function EmployeeDetails(props: Readonly<EmployeeDetailsProps>) {
    const [employee, setEmployee] = useState<EmployeeModel | null>(null);
    const {id} = useParams<{id: string}>();
    const navigate = useNavigate();
    const [showPopup, setShowPopup] = useState(false);
    const [deleteError, setDeleteError] = useState<string | null>(null);
    const [blockingAssignments, setBlockingAssignments] = useState<string[]>([]);
    const [showNoPermission, setShowNoPermission] = useState<boolean>(false);

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

                if (error.response?.status === 401) {
                    setDeleteError("You must be logged in as User/Admin to delete an employee.");
                    setBlockingAssignments([]);
                } else if (error.response?.status === 409) {
                    setDeleteError(error.response?.data?.message);
                    setBlockingAssignments(error.response?.data?.details ?? []);
                } else {
                    setDeleteError("Error deleting employee. Please try again.");
                    setBlockingAssignments([]);
                }
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
            <h2>Employee Details</h2>
            {employee ? (
                <div className="details-container">
                    {employee.imageUrl && (
                        <div className="details-img-container">
                            <img src={employee.imageUrl} alt={employee.name} className="details-image"/>
                        </div>
                    )}

                    <h3>Basic Information</h3>
                    <p><strong>Name:</strong> {employee.name}</p>
                    {employee.personnelNumber && <p><strong>Personnel Number:</strong> {employee.personnelNumber}</p>}
                    <p><strong>Department:</strong> {employee.department}</p>
                    <p><strong>Status:</strong> {employee.active ? 'Active' : 'Inactive'}</p>

                    <h3>Contact Information</h3>
                    {employee.email && <p><strong>Email:</strong> {employee.email}</p>}
                    {employee.phone && <p><strong>Phone:</strong> {employee.phone}</p>}
                    {employee.address && <p><strong>Address:</strong> {employee.address}</p>}

                    {employee.notes && (
                        <>
                            <h3>Notes</h3>
                            <p>{employee.notes}</p>
                        </>
                    )}

                    <h3>Metadata</h3>
                    <p><strong>ID:</strong> {employee.id}</p>

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
                                <h3>Confirm Deletion</h3>
                                <p>Are you sure you want to delete {employee.name}?</p>
                                {deleteError && (
                                    <div className="popup-error">
                                        <p>{deleteError}</p>
                                        {blockingAssignments.length > 0 && (
                                            <ul className="popup-error-list">
                                                {blockingAssignments.map((assignment) => (
                                                    <li key={assignment}>{assignment}</li>
                                                ))}
                                            </ul>
                                        )}
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
                <p>Loading...</p>
            )}
        </div>
    )
}