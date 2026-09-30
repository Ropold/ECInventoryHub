import {translatedInfo} from "../utils/TranslatedInfo.ts";
import type {LocationModel} from "../models/LocationModel.ts";
import {useNavigate, useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import axios from "axios";
import {handleRequestError, renderBlockingList} from "../utils/ComponentsFunctions.tsx";
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
                handleRequestError(error,
                    "You must be logged in as User/Admin to delete a location.",
                    "Error deleting location. Please try again.",
                    setDeleteError, setBlockingDevices);
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
            <h2>{translatedInfo["Location Details"][props.language]}</h2>
            {location ? (
                <div className="details-container">
                    {location.imageUrl && (
                        <div className="details-img-container">
                            <img src={location.imageUrl} alt={location.name} className="details-image"/>
                        </div>
                    )}

                    <h3>{translatedInfo["Basic Information"][props.language]}</h3>
                    <p><strong>{translatedInfo["Name"][props.language]}:</strong> {location.name}</p>

                    <h3>{translatedInfo["Contact Information"][props.language]}</h3>
                    {location.address && <p><strong>{translatedInfo["Address"][props.language]}:</strong> {location.address}</p>}
                    {location.phone && <p><strong>{translatedInfo["Phone"][props.language]}:</strong> {location.phone}</p>}
                    {location.email && <p><strong>{translatedInfo["Email"][props.language]}:</strong> {location.email}</p>}
                    {location.latitude != null && <p><strong>{translatedInfo["Latitude"][props.language]}:</strong> {location.latitude}</p>}
                    {location.longitude != null && <p><strong>{translatedInfo["Longitude"][props.language]}:</strong> {location.longitude}</p>}

                    {location.notes && (
                        <>
                            <h3>{translatedInfo["Notes"][props.language]}</h3>
                            <p>{location.notes}</p>
                        </>
                    )}

                    <h3>{translatedInfo["Metadata"][props.language]}</h3>
                    <p><strong>{translatedInfo["ID"][props.language]}:</strong> {location.id}</p>

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
                                <p>{translatedInfo["Delete confirmation"][props.language].replace("{name}", location.name)}</p>
                                {deleteError && (
                                    <div className="popup-error">
                                        <p>{deleteError}</p>
                                        {renderBlockingList(blockingDevices)}
                                    </div>
                                )}
                                <div className="popup-actions">
                                    <button onClick={handleConfirmDelete} className="popup-confirm">{translatedInfo["Yes, Delete"][props.language]}</button>
                                    {blockingDevices.length > 0 && props.role === "ADMIN" && (
                                        <button onClick={handleForceDelete} className="popup-confirm">{translatedInfo["Delete Anyway"][props.language]}</button>
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
