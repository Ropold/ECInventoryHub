import {useState} from "react";
import axios from "axios";
import {useNavigate} from "react-router-dom";
import type {AssignmentModel} from "../models/AssignmentModel.ts";
import AssignmentForm from "./AssignmentForm.tsx";

type AddNewAssignmentProps = {
    language: string;
    role: string;
    handleNewAssignmentSubmit: (newAssignment: AssignmentModel) => void;
}

export default function AddNewAssignment(props: Readonly<AddNewAssignmentProps>) {

    const [deviceId, setDeviceId] = useState<string>("");
    const [employeeId, setEmployeeId] = useState<string>("");
    const [handedOutById, setHandedOutById] = useState<string | undefined>(undefined);
    const [assignedDate, setAssignedDate] = useState<string>("");
    const [returnedDate, setReturnedDate] = useState<string | undefined>(undefined);
    const [conditionOut, setConditionOut] = useState<string | undefined>(undefined);
    const [conditionIn, setConditionIn] = useState<string | undefined>(undefined);
    const [notes, setNotes] = useState<string | undefined>(undefined);
    const [copyHandedToEmployee, setCopyHandedToEmployee] = useState<boolean>(false);
    const [copyFiledInPersonnelFile, setCopyFiledInPersonnelFile] = useState<boolean>(false);
    const [newFiles, setNewFiles] = useState<File[]>([]);

    const navigate = useNavigate();

    function handleNewAddSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();

        const newAssignment = {
            id: null,
            device: {id: deviceId},
            employee: {id: employeeId},
            handedOutBy: handedOutById ? {id: handedOutById} : null,
            assignedDate: assignedDate,
            returnedDate: returnedDate ?? null,
            conditionOut: conditionOut ?? null,
            conditionIn: conditionIn ?? null,
            notes: notes ?? null,
            copyHandedToEmployee: copyHandedToEmployee,
            copyFiledInPersonnelFile: copyFiledInPersonnelFile,
            files: []
        };

        const data = new FormData();
        data.append("assignmentDTO", new Blob([JSON.stringify(newAssignment)], {type: "application/json"}));
        newFiles.forEach((file) => data.append("files", file));

        axios
            .post('/api/assignments', data, {headers: {"Content-Type": "multipart/form-data"}})
            .then((response) => {
                props.handleNewAssignmentSubmit(response.data);
                navigate(`/assignments/${response.data.id}`);
            })
            .catch((error) => console.error("Error creating assignment", error));
    }

    const backNavigationPath = "/assignments";

    return (
        <div>
            <AssignmentForm
                language={props.language}
                backNavigationPath={backNavigationPath}
                handleSubmit={handleNewAddSubmit}
                {...{
                    deviceId, setDeviceId, employeeId, setEmployeeId,
                    handedOutById, setHandedOutById, assignedDate, setAssignedDate,
                    returnedDate, setReturnedDate, conditionOut, setConditionOut,
                    conditionIn, setConditionIn, notes, setNotes,
                    copyHandedToEmployee, setCopyHandedToEmployee, copyFiledInPersonnelFile, setCopyFiledInPersonnelFile,
                    newFiles, setNewFiles
                }}
            />
        </div>
    )
}
