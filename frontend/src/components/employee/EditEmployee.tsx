import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import axios from "axios";
import type {Department, EmployeeModel} from "../models/EmployeeModel.ts";
import EmployeeForm from "./EmployeeForm.tsx";

type EditEmployeeProps = {
    language: string;
    handleEmployeeUpdate: (updatedEmployee: EmployeeModel) => void;
}

export default function EditEmployee(props: Readonly<EditEmployeeProps>) {
    const [employee, setEmployee] = useState<EmployeeModel | null>(null);
    const {id} = useParams<{id: string}>();
    const navigate = useNavigate();

    const [personnelNumber, setPersonnelNumber] = useState<string | undefined>(undefined);
    const [name, setName] = useState<string>("");
    const [email, setEmail] = useState<string | undefined>(undefined);
    const [phone, setPhone] = useState<string | undefined>(undefined);
    const [address, setAddress] = useState<string | undefined>(undefined);
    const [department, setDepartment] = useState<Department>("MARKETING");
    const [active, setActive] = useState<boolean>(true);
    const [notes, setNotes] = useState<string | undefined>(undefined);
    const [image, setImage] = useState<File | null>(null);
    const [imageDeleted, setImageDeleted] = useState<boolean>(false);

    useEffect(() => {
        if (!id) return;
        axios
            .get(`/api/employees/${id}`)
            .then((response) => {
                const data: EmployeeModel = response.data;
                setEmployee(data);
                setPersonnelNumber(data.personnelNumber ?? undefined);
                setName(data.name);
                setEmail(data.email ?? undefined);
                setPhone(data.phone ?? undefined);
                setAddress(data.address ?? undefined);
                setDepartment(data.department);
                setActive(data.active);
                setNotes(data.notes ?? undefined);
            })
            .catch((error) => console.error("Error fetching employee details", error));
    }, [id]);

    function handleSaveEdit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        if (!employee) return;

        const updatedEmployee = {
            id: employee.id,
            personnelNumber: personnelNumber ?? null,
            name: name,
            email: email ?? null,
            phone: phone ?? null,
            address: address ?? null,
            department: department,
            active: active,
            notes: notes ?? null,
            imageUrl: imageDeleted ? null : employee.imageUrl
        };

        const data = new FormData();
        data.append("employeeDTO", new Blob([JSON.stringify(updatedEmployee)], {type: "application/json"}));
        if (image) {
            data.append("image", image);
        }

        axios
            .put(`/api/employees/${employee.id}`, data, {headers: {"Content-Type": "multipart/form-data"}})
            .then((response) => {
                props.handleEmployeeUpdate(response.data);
                navigate(`/employees/${employee.id}`);
            })
            .catch((error) => console.error("Error updating employee", error));
    }

    const backNavigationPath = employee?.id ? `/employees/${employee.id}` : "/employees";

    return (
        <div>
            <EmployeeForm
                language={props.language}
                backNavigationPath={backNavigationPath}
                handleSubmit={handleSaveEdit}
                personnelNumber={personnelNumber}
                setPersonnelNumber={setPersonnelNumber}
                name={name}
                setName={setName}
                email={email}
                setEmail={setEmail}
                phone={phone}
                setPhone={setPhone}
                address={address}
                setAddress={setAddress}
                department={department}
                setDepartment={setDepartment}
                active={active}
                setActive={setActive}
                notes={notes}
                setNotes={setNotes}
                image={image}
                setImage={setImage}
                existingImageUrl={employee?.imageUrl ?? undefined}
                imageDeleted={imageDeleted}
                setImageDeleted={setImageDeleted}
            />
        </div>
    )
}
