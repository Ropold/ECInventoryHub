import type {AssignmentModel} from "../models/AssignmentModel.ts";
import {useNavigate} from "react-router-dom";
import "../styles/assignment/AssignmentCard.css";

type AssignmentCardProps = {
    assignment: AssignmentModel;
    language: string;
}

export default function AssignmentCard(props: Readonly<AssignmentCardProps>){
    const navigate = useNavigate();

    const handleCardClick = () => {
        navigate(`/assignments/${props.assignment.id}`);
    }

    return (
        <button type="button" className="assignment-card" onClick={handleCardClick}>
            <h2>{props.assignment.employee.name}</h2>
            <p className="assignment-card-device">
                {[props.assignment.device.manufacturer, props.assignment.device.modelName].filter(Boolean).join(" ")}
            </p>
            <p className="assignment-card-date">{props.assignment.assignedDate}</p>
        </button>
    )
}
