import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import axios from "axios";
import type {AssignmentModel} from "../models/AssignmentModel.ts";
import type {AssignmentFileModel} from "../models/AssignmentFileModel.ts";
import AssignmentForm from "./AssignmentForm.tsx";

type EditAssignmentProps = {
    language: string;
    handleAssignmentUpdate: (updatedAssignment: AssignmentModel) => void;
}

export default function EditAssignment(props: Readonly<EditAssignmentProps>) {
    const [assignment, setAssignment] = useState<AssignmentModel | null>(null);
    const {id} = useParams<{id: string}>();
    const navigate = useNavigate();

    const [deviceId, setDeviceId] = useState<string>("");
    const [employeeId, setEmployeeId] = useState<string>("");
    const [handedOutById, setHandedOutById] = useState<string>();
    const [assignedDate, setAssignedDate] = useState<string>("");
    const [returnedDate, setReturnedDate] = useState<string>();
    const [conditionOut, setConditionOut] = useState<string>();
    const [conditionIn, setConditionIn] = useState<string>();
    const [notes, setNotes] = useState<string>();
    const [copyHandedToEmployee, setCopyHandedToEmployee] = useState<boolean>(false);
    const [copyFiledInPersonnelFile, setCopyFiledInPersonnelFile] = useState<boolean>(false);
    const [newFiles, setNewFiles] = useState<File[]>([]);
    const [existingFiles, setExistingFiles] = useState<AssignmentFileModel[]>([]);

    useEffect(() => {
        if (!id) return;
        axios
            .get(`/api/assignments/${id}`)
            .then((response) => {
                const data: AssignmentModel = response.data;
                setAssignment(data);
                setDeviceId(data.device.id);
                setEmployeeId(data.employee.id);
                setHandedOutById(data.handedOutBy?.id ?? undefined);
                setAssignedDate(data.assignedDate);
                setReturnedDate(data.returnedDate ?? undefined);
                setConditionOut(data.conditionOut ?? undefined);
                setConditionIn(data.conditionIn ?? undefined);
                setNotes(data.notes ?? undefined);
                setCopyHandedToEmployee(data.copyHandedToEmployee);
                setCopyFiledInPersonnelFile(data.copyFiledInPersonnelFile);
                setExistingFiles(data.files ?? []);
            })
            .catch((error) => console.error("Error fetching assignment details", error));
    }, [id]);

    function handleSaveEdit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        if (!assignment) return;

        const updatedAssignment = {
            id: assignment.id,
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
            files: existingFiles
        };

        const data = new FormData();
        data.append("assignmentDTO", new Blob([JSON.stringify(updatedAssignment)], {type: "application/json"}));
        newFiles.forEach((file) => data.append("files", file));

        axios
            .put(`/api/assignments/${assignment.id}`, data, {headers: {"Content-Type": "multipart/form-data"}})
            .then((response) => {
                props.handleAssignmentUpdate(response.data);
                navigate(`/assignments/${assignment.id}`);
            })
            .catch((error) => console.error("Error updating assignment", error));
    }

    const backNavigationPath = assignment?.id ? `/assignments/${assignment.id}` : "/assignments";

    return (
        <div>
            <AssignmentForm
                language={props.language}
                backNavigationPath={backNavigationPath}
                handleSubmit={handleSaveEdit}
                {...{
                    deviceId, setDeviceId, employeeId, setEmployeeId,
                    handedOutById, setHandedOutById, assignedDate, setAssignedDate,
                    returnedDate, setReturnedDate, conditionOut, setConditionOut,
                    conditionIn, setConditionIn, notes, setNotes,
                    copyHandedToEmployee, setCopyHandedToEmployee, copyFiledInPersonnelFile, setCopyFiledInPersonnelFile,
                    newFiles, setNewFiles, existingFiles, setExistingFiles
                }}
            />
        </div>
    )
}
