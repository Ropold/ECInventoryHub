import type {EmployeeModel} from "../models/EmployeeModel.ts";
import {useNavigate} from "react-router-dom";
import "../styles/employee/EmployeeCard.css";

type EmployeeCardProps = {
    employee: EmployeeModel;
    language: string;
}

export default function EmployeeCard(props: Readonly<EmployeeCardProps>){
    const navigate = useNavigate();

    const handleCardClick = () => {
        navigate(`/employees/${props.employee.id}`);
    }

    return (
        <div className="employee-card" onClick={handleCardClick}>
            <h2>{props.employee.name}</h2>
            {props.employee.imageUrl && (
                <img
                    className="employee-card-image"
                    src={props.employee.imageUrl}
                    alt={props.employee.name}
                />
            )}
        </div>
    )
}