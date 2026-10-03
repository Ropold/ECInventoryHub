import type {AssignmentModel} from "../models/AssignmentModel.ts";
import type {DeviceType} from "../models/DeviceModel.ts";
import {useNavigate} from "react-router-dom";
import {formatDate, getDeviceLabel} from "../utils/ComponentsFunctions.tsx";
import {translatedInfo} from "../utils/TranslatedInfo.ts";
import "../styles/assignment/AssignmentCard.css";

type AssignmentCardProps = {
    assignment: AssignmentModel;
    language: string;
    hideEmployee?: boolean;
}

const deviceTypeIcons: Record<DeviceType, string> = {
    LAPTOP: "💻",
    PHONE: "📱",
    TABLET: "📱",
    MONITOR: "🖥️",
    ACCESSORY: "🖱️",
    OTHER: "📦",
};

export default function AssignmentCard(props: Readonly<AssignmentCardProps>){
    const navigate = useNavigate();

    const handleCardClick = () => {
        navigate(`/assignments/${props.assignment.id}`);
    }

    const {device, employee, assignedDate, returnedDate} = props.assignment;
    const isActive = !returnedDate;
    const deviceLabel = `${deviceTypeIcons[device.type]} ${getDeviceLabel(device)}`;
    const deviceName = [device.manufacturer, device.modelName].filter(Boolean).join(" ");
    const documentsMissing = !props.assignment.copyHandedToEmployee || !props.assignment.copyFiledInPersonnelFile;
    const dateText = isActive
        ? formatDate(assignedDate)
        : `${formatDate(assignedDate)} – ${formatDate(returnedDate ?? undefined)}`;

    return (
        <button
            type="button"
            className={`assignment-card${isActive ? "" : " assignment-card-returned"}`}
            onClick={handleCardClick}
        >
            {documentsMissing && (
                <span
                    className="assignment-card-warning"
                    title={translatedInfo["Documents missing"][props.language]}
                >
                    ⚠️
                </span>
            )}
            {props.hideEmployee ? (
                <h2>{deviceLabel}</h2>
            ) : (
                <>
                    <h2>{employee.name}</h2>
                    <p className="assignment-card-label">{deviceLabel}</p>
                </>
            )}
            {deviceName && <p className="assignment-card-device">{deviceName}</p>}
            <p className="assignment-card-date">{dateText}</p>
            <span className={`assignment-card-badge ${isActive ? "assignment-card-badge-active" : "assignment-card-badge-returned"}`}>
                {translatedInfo[isActive ? "Active" : "Returned"][props.language]}
            </span>
        </button>
    )
}
