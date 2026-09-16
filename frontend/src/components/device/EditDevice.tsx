import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import axios from "axios";
import type {DeviceModel, DeviceStatus, DeviceType} from "../models/DeviceModel.ts";
import type {DeviceFileModel} from "../models/DeviceFileModel.ts";
import DeviceForm from "./DeviceForm.tsx";

type EditDeviceProps = {
    language: string;
    handleDeviceUpdate: (updatedDevice: DeviceModel) => void;
}

export default function EditDevice(props: Readonly<EditDeviceProps>){
    const [device, setDevice] = useState<DeviceModel | null>(null);
    const {id} = useParams<{id: string}>();
    const navigate = useNavigate();

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
    const [existingFiles, setExistingFiles] = useState<DeviceFileModel[]>([]);

    useEffect(() => {
        if (!id) return;
        axios
            .get(`/api/devices/${id}`)
            .then((response) => {
                const data: DeviceModel = response.data;
                setDevice(data);
                setType(data.type);
                setManufacturer(data.manufacturer ?? undefined);
                setModelName(data.modelName ?? undefined);
                setSerialNumber(data.serialNumber ?? undefined);
                setInventoryNumber(data.inventoryNumber ?? undefined);
                setPurchaseDate(data.purchaseDate ?? undefined);
                setStatus(data.status);
                setDefective(data.defective);
                setLocationId(data.location?.id ?? undefined);
                setNotes(data.notes ?? undefined);
                setExistingFiles(data.files ?? []);
            })
            .catch((error) => console.error("Error fetching device details", error));
    }, [id]);

    function handleSaveEdit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        if (!device) return;

        const updatedDevice = {
            id: device.id,
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
            files: existingFiles
        };

        const data = new FormData();
        data.append("deviceDTO", new Blob([JSON.stringify(updatedDevice)], {type: "application/json"}));
        newFiles.forEach((file) => data.append("files", file));

        axios
            .put(`/api/devices/${device.id}`, data, {headers: {"Content-Type": "multipart/form-data"}})
            .then((response) => {
                props.handleDeviceUpdate(response.data);
                navigate(`/devices/${device.id}`);
            })
            .catch((error) => console.error("Error updating device", error));
    }

    const backNavigationPath = device?.id ? `/devices/${device.id}` : "/devices";

    return (
        <div>
            <DeviceForm
                language={props.language}
                backNavigationPath={backNavigationPath}
                handleSubmit={handleSaveEdit}
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
                existingFiles={existingFiles}
                setExistingFiles={setExistingFiles}
            />
        </div>
    )
}
