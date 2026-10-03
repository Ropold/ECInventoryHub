import type {DeviceModel} from "../models/DeviceModel.ts";
import {useNavigate} from "react-router-dom";
import CardWarning from "../CardWarning.tsx";
import {getDeviceLabel} from "../utils/ComponentsFunctions.tsx";
import {deviceStatusBadgeClasses, deviceStatusLabelKeys, deviceTypeIcons} from "../utils/DeviceFilters.ts";
import {translatedInfo} from "../utils/TranslatedInfo.ts";
import "../styles/device/DeviceCard.css";
import "../styles/CardBadge.css";

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
    const identifiers = [
        {labelKey: "Inventory Number", value: props.device.inventoryNumber},
        {labelKey: "Hostname", value: props.device.hostname},
        {labelKey: "Serial Number", value: props.device.serialNumber},
    ].filter((identifier) => identifier.value);

    return (
        <button type="button" className="device-card" onClick={handleCardClick}>
            {props.device.defective && (
                <CardWarning
                    language={props.language}
                    title={translatedInfo["Defective"][props.language]}
                    items={props.device.notes ? [props.device.notes] : []}
                />
            )}
            <h2 className="card-title">{deviceTypeIcons[props.device.type]} {getDeviceLabel(props.device)}</h2>
            {deviceName && <p className="card-subtitle">{deviceName}</p>}
            {identifiers.map((identifier) => (
                <p key={identifier.labelKey} className="card-detail">
                    {translatedInfo[identifier.labelKey][props.language]}: {identifier.value}
                </p>
            ))}
            {imageFile && (
                <img
                    className="device-card-image"
                    src={imageFile.fileUrl}
                    alt={deviceName}
                />
            )}
            <span className={`card-badge ${deviceStatusBadgeClasses[props.device.status]}`}>
                {translatedInfo[deviceStatusLabelKeys[props.device.status]][props.language]}
            </span>
        </button>
    )
}
