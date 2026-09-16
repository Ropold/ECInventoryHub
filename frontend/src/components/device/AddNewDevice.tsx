import {useState} from "react";
import axios from "axios";
import {useNavigate} from "react-router-dom";
import type {DeviceModel, DeviceStatus, DeviceType} from "../models/DeviceModel.ts";
import DeviceForm from "./DeviceForm.tsx";

type AddNewDeviceProps = {
    language: string;
    role: string;
    handleNewDeviceSubmit: (newDevice: DeviceModel) => void;
}

export default function AddNewDevice(props: Readonly<AddNewDeviceProps>) {

    const [type, setType] = useState<DeviceType>("LAPTOP");
    const [manufacturer, setManufacturer] = useState<string | undefined>(undefined);
    const [modelName, setModelName] = useState<string | undefined>(undefined);
    const [serialNumber, setSerialNumber] = useState<string | undefined>(undefined);
    const [inventoryNumber, setInventoryNumber] = useState<string | undefined>(undefined);
    const [purchaseDate, setPurchaseDate] = useState<string | undefined>(undefined);
    const [status, setStatus] = useState<DeviceStatus>("AVAILABLE");
    const [defective, setDefective] = useState<boolean>(false);
    const [locationId, setLocationId] = useState<string | undefined>(undefined);
    const [notes, setNotes] = useState<string | undefined>(undefined);
    const [newFiles, setNewFiles] = useState<File[]>([]);

    const navigate = useNavigate();

    function handleNewAddSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();

        const newDevice = {
            id: null,
            type: type,
            manufacturer: manufacturer ?? null,
            modelName: modelName ?? null,
            serialNumber: serialNumber ?? null,
            inventoryNumber: inventoryNumber ?? null,
            purchaseDate: purchaseDate ?? null,
            status: status,
            defective: defective,
            location: locationId ? {id: locationId} : null,
            notes: notes ?? null,
            files: []
        };

        const data = new FormData();
        data.append("deviceDTO", new Blob([JSON.stringify(newDevice)], {type: "application/json"}));
        newFiles.forEach((file) => data.append("files", file));

        axios
            .post('/api/devices', data, {headers: {"Content-Type": "multipart/form-data"}})
            .then((response) => {
                props.handleNewDeviceSubmit(response.data);
                navigate(`/devices/${response.data.id}`);
            })
            .catch((error) => console.error("Error creating device", error));
    }

    const backNavigationPath = "/devices";

    return (
        <div>
            <DeviceForm
                language={props.language}
                backNavigationPath={backNavigationPath}
                handleSubmit={handleNewAddSubmit}
                type={type}
                setType={setType}
                manufacturer={manufacturer}
                setManufacturer={setManufacturer}
                modelName={modelName}
                setModelName={setModelName}
                serialNumber={serialNumber}
                setSerialNumber={setSerialNumber}
                inventoryNumber={inventoryNumber}
                setInventoryNumber={setInventoryNumber}
                purchaseDate={purchaseDate}
                setPurchaseDate={setPurchaseDate}
                status={status}
                setStatus={setStatus}
                defective={defective}
                setDefective={setDefective}
                locationId={locationId}
                setLocationId={setLocationId}
                notes={notes}
                setNotes={setNotes}
                newFiles={newFiles}
                setNewFiles={setNewFiles}
            />
        </div>
    )
}
