import {useState} from "react";
import axios from "axios";
import {useNavigate} from "react-router-dom";
import type {Department, EmployeeModel} from "../models/EmployeeModel.ts";
import EmployeeForm from "./EmployeeForm.tsx";

type AddNewEmployeeProps = {
    language: string;
    role: string;
    handleNewEmployeeSubmit: (newEmployee: EmployeeModel) => void;
}

export default function AddNewEmployee(props: Readonly<AddNewEmployeeProps>) {

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

    const navigate = useNavigate();

    function handleNewAddSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();

        const newEmployee = {
            id: null,
            personnelNumber: personnelNumber ?? null,
            name: name,
            email: email ?? null,
            phone: phone ?? null,
            address: address ?? null,
            department: department,
            active: active,
            notes: notes ?? null,
            imageUrl: null
        };

        const data = new FormData();
        data.append("employeeDTO", new Blob([JSON.stringify(newEmployee)], {type: "application/json"}));
        if (image) {
            data.append("image", image);
        }

        axios
            .post('/api/employees', data, {headers: {"Content-Type": "multipart/form-data"}})
            .then((response) => {
                props.handleNewEmployeeSubmit(response.data);
                navigate(`/employees/${response.data.id}`);
            })
            .catch((error) => console.error("Error creating employee", error));
    }

    const backNavigationPath = "/employees";

    return (
        <div>
            <EmployeeForm
                language={props.language}
                backNavigationPath={backNavigationPath}
                handleSubmit={handleNewAddSubmit}
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
                imageDeleted={imageDeleted}
                setImageDeleted={setImageDeleted}
            />
        </div>
    )
}
