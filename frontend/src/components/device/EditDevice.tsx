import type {DeviceModel} from "../models/DeviceModel.ts";

type EditDeviceProps = {
    language: string;
    handleDeviceUpdate: (updatedDevice: DeviceModel) => void;
}

export default function EditDevice(props: Readonly<EditDeviceProps>){
    return(
        <>
            <h2>Edit Device</h2>
            <p>{props.language}</p>
        </>
    )
}
