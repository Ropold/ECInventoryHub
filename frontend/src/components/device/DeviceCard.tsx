import type {DeviceModel} from "../models/DeviceModel.ts";
import {useNavigate} from "react-router-dom";
import "../styles/device/DeviceCard.css";

type DeviceCardProps = {
    device: DeviceModel;
    language: string;
}

export default function DeviceCard(props: Readonly<DeviceCardProps>){
    const navigate = useNavigate();

    const handleCardClick = () => {
        navigate(`/devices/${props.device.id}`);
    }

    const deviceName = [props.device.manufacturer, props.device.modelName].filter(Boolean).join(" ");
    const imageFile = props.device.files.find((file) => file.fileType?.startsWith("image/"));

    return (
        <button type="button" className="device-card" onClick={handleCardClick}>
            <h2>{deviceName}</h2>
            {imageFile && (
                <img
                    className="device-card-image"
                    src={imageFile.fileUrl}
                    alt={deviceName}
                />
            )}
            <p className="device-card-inventory">{props.device.inventoryNumber}</p>
            <p className="device-card-status">{props.device.status}</p>
        </button>
    )
}
