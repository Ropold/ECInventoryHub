import {translatedInfo} from "../utils/TranslatedInfo.ts";
import type {DeviceModel} from "../models/DeviceModel.ts";
import {useNavigate, useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import axios from "axios";
import {formatDate} from "../utils/ComponentsFunctions.tsx";
import "../styles/Details.css";
import NoPermissionPopup from "../NoPermissionPopup.tsx";

type DeviceDetailsProps = {
    language: string;
    role: string;
    handleDeviceUpdate: (updatedDevice: DeviceModel) => void;
    handleDeviceDelete: (deletedDeviceId: string) => void;
}

export default function DeviceDetails(props: Readonly<DeviceDetailsProps>) {
    const [device, setDevice] = useState<DeviceModel | null>(null);
    const {id} = useParams<{id: string}>();
    const navigate = useNavigate();
    const [showPopup, setShowPopup] = useState(false);
    const [deleteError, setDeleteError] = useState<string | null>(null);
    const [showNoPermission, setShowNoPermission] = useState<boolean>(false);

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

                if (error.response?.status === 401 || error.response?.status === 403) {
                    setDeleteError("You must be logged in as User/Admin to delete a device.");
                } else if (error.response?.status === 500) {
                    setDeleteError("This device cannot be deleted while assignments still reference it.");
                } else {
                    setDeleteError("Error deleting device. Please try again.");
                }
            })
    }

    function handleCancel() {
        setShowPopup(false);
        setDeleteError(null);
    }

    const deviceName = device
        ? [device.manufacturer, device.modelName].filter(Boolean).join(" ")
        : "";

    return (
        <div>
            <h2>Device Details</h2>
            {device ? (
                <div className="details-container">
                    <h3>Basic Information</h3>
                    <p><strong>Device:</strong> {deviceName}</p>
                    <p><strong>Type:</strong> {device.type}</p>
                    <p><strong>Status:</strong> {device.status}</p>
                    <p><strong>Defective:</strong> {device.defective ? 'Yes' : 'No'}</p>

                    <h3>Identification</h3>
                    {device.inventoryNumber && <p><strong>Inventory Number:</strong> {device.inventoryNumber}</p>}
                    {device.serialNumber && <p><strong>Serial Number:</strong> {device.serialNumber}</p>}
                    <p><strong>Purchase Date:</strong> {formatDate(device.purchaseDate ?? undefined)}</p>

                    {device.location && (
                        <>
                            <h3>Location</h3>
                            <p><strong>Name:</strong> {device.location.name}</p>
                            {device.location.address && <p><strong>Address:</strong> {device.location.address}</p>}
                            {device.location.phone && <p><strong>Phone:</strong> {device.location.phone}</p>}
                            {device.location.email && <p><strong>Email:</strong> {device.location.email}</p>}
                        </>
                    )}

                    {device.notes && (
                        <>
                            <h3>Notes</h3>
                            <p>{device.notes}</p>
                        </>
                    )}

                    {device.files.length > 0 && (
                        <>
                            <h3>Files</h3>
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

                    <h3>Metadata</h3>
                    <p><strong>ID:</strong> {device.id}</p>

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
                                <p>Are you sure you want to delete {deviceName}?</p>
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
