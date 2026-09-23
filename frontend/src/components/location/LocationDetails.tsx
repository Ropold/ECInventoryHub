import type {LocationModel} from "../models/LocationModel.ts";
import {useNavigate, useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import axios from "axios";
import "../styles/Details.css";
import NoPermissionPopup from "../NoPermissionPopup.tsx";

type LocationDetailsProps = {
    language: string;
    role: string;
    handleLocationUpdate: (updatedLocation: LocationModel) => void;
    handleLocationDelete: (deletedLocationId: string) => void;
}

export default function LocationDetails(props: Readonly<LocationDetailsProps>) {
    const [location, setLocation] = useState<LocationModel | null>(null);
    const {id} = useParams<{id: string}>();
    const navigate = useNavigate();
    const [showPopup, setShowPopup] = useState(false);
    const [deleteError, setDeleteError] = useState<string | null>(null);
    const [blockingDevices, setBlockingDevices] = useState<string[]>([]);
    const [showNoPermission, setShowNoPermission] = useState<boolean>(false);

    function handleEditClick() {
        if (props.role === "VIEWER") {
            setShowNoPermission(true);
            return;
        }
        navigate(`/locations/${id}/edit`);
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
            .get(`/api/locations/${id}`)
            .then((response) => setLocation(response.data))
            .catch((error) => console.error("Error fetching location details", error));
    }, [id]);

    function handleConfirmDelete(){
        if(!location) return;

        axios
            .delete(`/api/locations/${id}`)
            .then(() => {
                console.log("Successfully deleted location");
                props.handleLocationDelete(location.id);
                setShowPopup(false);
                navigate("/locations");
            })
            .catch((error) => {
                console.error("Error deleting location", error);

                if (error.response?.status === 401) {
                    setDeleteError("You must be logged in as User/Admin to delete a location.");
                    setBlockingDevices([]);
                } else if (error.response?.status === 409) {
                    setDeleteError(error.response?.data?.message);
                    setBlockingDevices(error.response?.data?.details ?? []);
                } else {
                    setDeleteError("Error deleting location. Please try again.");
                    setBlockingDevices([]);
                }
            })
    }

    function handleForceDelete(){
        if(!location) return;

        axios
            .delete(`/api/locations/${id}/force`)
            .then(() => {
                console.log("Successfully deleted location and detached all devices");
                props.handleLocationDelete(location.id);
                setShowPopup(false);
                navigate("/locations");
            })
            .catch((error) => {
                console.error("Error force deleting location", error);

                if (error.response?.status === 401) {
                    setDeleteError("You must be logged in as Admin to delete a location with devices.");
                } else if (error.response?.status === 403) {
                    setDeleteError("You must be an Admin to delete a location with devices.");
                } else {
                    setDeleteError("Error deleting location. Please try again.");
                }
            })
    }

    function handleCancel(){
        setShowPopup(false);
        setDeleteError(null);
        setBlockingDevices([]);
    }

    return(
        <div>
            <h2>Location Details</h2>
            {location ? (
                <div className="details-container">
                    {location.imageUrl && (
                        <div className="details-img-container">
                            <img src={location.imageUrl} alt={location.name} className="details-image"/>
                        </div>
                    )}

                    <h3>Basic Information</h3>
                    <p><strong>Name:</strong> {location.name}</p>

                    <h3>Contact Information</h3>
                    {location.address && <p><strong>Address:</strong> {location.address}</p>}
                    {location.phone && <p><strong>Phone:</strong> {location.phone}</p>}
                    {location.email && <p><strong>Email:</strong> {location.email}</p>}

                    {location.notes && (
                        <>
                            <h3>Notes</h3>
                            <p>{location.notes}</p>
                        </>
                    )}

                    <h3>Metadata</h3>
                    <p><strong>ID:</strong> {location.id}</p>

                    <div className="details-buttons">
                        <button className="button-blue" onClick={handleEditClick}>Edit</button>
                        <button className="button-delete" onClick={handleDeleteClick}>Delete</button>
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
                                <p>Are you sure you want to delete {location.name}?</p>
                                {deleteError && (
                                    <div className="popup-error">
                                        <p>{deleteError}</p>
                                        {blockingDevices.length > 0 && (
                                            <ul className="popup-error-list">
                                                {blockingDevices.map((device) => (
                                                    <li key={device}>{device}</li>
                                                ))}
                                            </ul>
                                        )}
                                    </div>
                                )}
                                <div className="popup-actions">
                                    <button onClick={handleConfirmDelete} className="popup-confirm">Yes, Delete</button>
                                    {blockingDevices.length > 0 && props.role === "ADMIN" && (
                                        <button onClick={handleForceDelete} className="popup-confirm">Delete Anyway</button>
                                    )}
                                    <button onClick={handleCancel} className="popup-cancel">Cancel</button>
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
